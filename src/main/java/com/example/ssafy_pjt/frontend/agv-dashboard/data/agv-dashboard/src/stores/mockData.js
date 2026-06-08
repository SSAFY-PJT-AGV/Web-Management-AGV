export const mock = {
  agvs:[{agvId:'AGV01',status:'MOVING',currentJob:'자재 공급 작업',cargo:'CHIP',marker:301,waitingMissionCount:2},{agvId:'AGV02',status:'IDLE',currentJob:'대기 중',cargo:null,marker:201,waitingMissionCount:1}],
  missions:[{order:1,jobName:'자재 공급 작업',agv:'AGV01',status:'진행중'},{order:2,jobName:'컨베이어 이동 작업',agv:'AGV01',status:'대기'},{order:1,jobName:'완제품 회수 작업',agv:'AGV02',status:'대기'}],
  inventory:[{material:'CHIP',label:'칩',currentQuantity:8,minThreshold:10,status:'LOW'},{material:'SENSOR',label:'센서',currentQuantity:31,minThreshold:10,status:'NORMAL'},{material:'BATTERY',label:'배터리 부품',currentQuantity:5,minThreshold:8,status:'LOW'}],
  recommendations:[{title:'칩 재고 선제 보급 권장',message:'최근 생산 요청 기준 CHIP 소진 속도가 빠릅니다.',level:'WARN'}],
  events:[{time:'14:20:11',level:'INFO',message:'Dashboard mock mode started'},{time:'14:22:03',level:'WARN',message:'CHIP 재고 부족 예상'}],
  map:{markers:[{markerId:101,x:86,y:390,zoneName:'컨베이어',type:'CONVEYOR'},{markerId:201,x:410,y:140,zoneName:'AGV02 시작',type:'START'},{markerId:301,x:755,y:785,zoneName:'AGV01 시작',type:'START'},{markerId:401,x:505,y:420,zoneName:'완제품 상자 보관 구역',type:'FINISHED'},{markerId:501,x:500,y:650,zoneName:'자재 상자 보관 구역',type:'STORAGE'},{markerId:601,x:810,y:520,zoneName:'교차 구역',type:'CROSS'},{markerId:701,x:825,y:110,zoneName:'입출고구역',type:'INOUT'}],agvs:[{agvId:'AGV01',currentMarker:301,status:'MOVING'},{agvId:'AGV02',currentMarker:201,status:'IDLE'}]}
}
