import asyncio
import websockets
import json
import time
import argparse


async def agv_client(agv_id, fps, duration):

    uri = "ws://localhost:8080/ws/agv"

    async with websockets.connect(uri) as ws:

        end = time.time() + duration

        count = 0

        while time.time() < end:

            msg = {
                "agvId": agv_id,
                "timestamp": int(time.time()),
                "status": "MOVING",
                "event": "STATUS",
                "taskId": None,
                "commandId": None,
                "located": 1,
                "destination": 2,
                "cargo": "NONE",
                "isCW": False,
                "hasImage": False,
                "image": None
            }

            await ws.send(json.dumps(msg))

            count += 1

            await asyncio.sleep(1 / fps)

        print(f"AGV {agv_id}: {count} messages")


async def main():

    parser = argparse.ArgumentParser()

    parser.add_argument("--agvs", type=int)
    parser.add_argument("--fps", type=int)
    parser.add_argument("--duration", type=int)

    args = parser.parse_args()


    tasks = []

    for i in range(1, args.agvs + 1):
        tasks.append(
            agv_client(
                i,
                args.fps,
                args.duration
            )
        )


    await asyncio.gather(*tasks)


asyncio.run(main())