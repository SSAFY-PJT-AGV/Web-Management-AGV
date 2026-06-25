import argparse
import base64
import json
import time

from tornado import gen
from tornado.ioloop import IOLoop
from tornado.websocket import websocket_connect


SERVER_WS_URL = "ws://localhost:8080/ws/agv"

STATUS_INTERVAL_SEC = 0.1
ASSIGNED_SECONDS = 1.5
MOVING_SECONDS = 5.0
IDLE_DELAY_SECONDS = 1.0

SEND_DUMMY_IMAGE = True
DUMMY_IMAGE_BYTES = 4500


class FakeAgv:

    def __init__(self, agv_id):
        self.agv_id = agv_id
        self.loop = IOLoop.current()
        self.ws = None

        self.running = False
        self.simulating = False

        self.status = "IDLE"
        self.event = "NONE"

        self.task_id = None
        self.command_id = None
        self.destination = None
        self.located = None
        self.cargo = None

        self.dummy_image = base64.b64encode(
            b"0" * DUMMY_IMAGE_BYTES
        ).decode()

    def start(self):
        self.running = True
        self.loop.add_callback(self.connect)
        print(f"[FAKE AGV{self.agv_id} START]")

    @gen.coroutine
    def connect(self):
        try:
            self.ws = yield websocket_connect(SERVER_WS_URL)
            print(f"[AGV{self.agv_id} CONNECTED]")

            yield self.send_status_once(event="CONNECTED")

            self.loop.add_callback(self.receive_loop)
            self.loop.add_callback(self.status_loop)

        except Exception as e:
            print("[CONNECT ERROR]", e)

    @gen.coroutine
    def receive_loop(self):
        while self.running:
            try:
                msg = yield self.ws.read_message()

                if msg is None:
                    print("[SERVER CLOSED]")
                    break

                data = json.loads(msg)
                msg_type = data.get("messageType") or data.get("type")

                if msg_type == "COMMAND_ASSIGN":
                    self.handle_command_assign(data)
                elif msg_type == "ERROR":
                    print("[SERVER ERROR]", data)
                else:
                    print("[RECV]", data)

            except Exception as e:
                print("[RECEIVE ERROR]", e)
                break

    def handle_command_assign(self, data):
        if self.simulating:
            print("[COMMAND IGNORE] already running")
            return

        self.command_id = data.get("commandId")
        self.task_id = data.get("taskId")
        self.destination = data.get("destination")
        self.cargo = data.get("cargo")

        print("====================")
        print(f"AGV{self.agv_id} COMMAND_ASSIGN")
        print("cmd :", self.command_id)
        print("task:", self.task_id)
        print("dest:", self.destination)
        print("cargo:", self.cargo)
        print("====================")

        self.loop.add_callback(self.simulate_flow)

    @gen.coroutine
    def simulate_flow(self):
        if self.simulating:
            return

        self.simulating = True
        finished_command_id = self.command_id

        try:
            self.status = "ASSIGNED"
            self.event = "COMMAND_RECEIVED"

            for _ in range(max(1, int(ASSIGNED_SECONDS / STATUS_INTERVAL_SEC))):
                yield self.send_status_once()
                yield gen.sleep(STATUS_INTERVAL_SEC)

            self.status = "MOVING"
            self.event = "MOVING_TO_DESTINATION"

            steps = max(1, int(MOVING_SECONDS / STATUS_INTERVAL_SEC))

            for _ in range(steps):
                yield self.send_status_once()
                yield gen.sleep(STATUS_INTERVAL_SEC)

            self.located = self.destination
            self.status = "DONE"
            self.event = "DONE"

            yield self.send_status_once()

            print(f"[AGV{self.agv_id} DONE] commandId={finished_command_id}")

            self.status = "IDLE_WAIT"
            self.event = "NONE"

            yield gen.sleep(IDLE_DELAY_SECONDS)

            self.status = "IDLE"
            self.event = "NONE"

            self.command_id = None
            self.task_id = None
            self.cargo = None
            # destination은 유지: 목적지 표시 깜빡임 방지
            # located도 내부에는 유지하지만 IDLE 메시지에서는 보내지 않음

            yield self.send_status_once()

            print(f"[AGV{self.agv_id} IDLE] after commandId={finished_command_id}")

        finally:
            self.simulating = False

    @gen.coroutine
    def status_loop(self):
        while self.running:
            if self.status != "IDLE_WAIT":
                yield self.send_status_once()

            yield gen.sleep(STATUS_INTERVAL_SEC)

    @gen.coroutine
    def send_status_once(self, event=None):
        if self.ws is None:
            return

        image = self.dummy_image if SEND_DUMMY_IMAGE else None

        located_value = self.located

        if self.status == "IDLE":
            located_value = None

        payload = {
            "agvId": self.agv_id,
            "status": self.status,
            "event": event if event is not None else self.event,
            "taskId": self.task_id,
            "commandId": self.command_id,
            "located": located_value,
            "destination": self.destination,
            "cargo": self.cargo,
            "hasImage": SEND_DUMMY_IMAGE,
            "image": image,
            "timestamp": int(time.time() * 1000)
        }

        try:
            yield self.ws.write_message(json.dumps(payload))
        except Exception as e:
            print("[SEND ERROR]", e)

    def stop(self):
        self.running = False

        try:
            if self.ws:
                self.ws.close()
        except Exception:
            pass

        print(f"[AGV{self.agv_id} STOP]")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--agv-id", type=int, required=True)
    args = parser.parse_args()

    client = FakeAgv(args.agv_id)
    client.start()

    try:
        IOLoop.current().start()
    except KeyboardInterrupt:
        client.stop()