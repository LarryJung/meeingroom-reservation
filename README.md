# meeingroom-reservation

## 회의실 예약 프로그램  
![Screenshot from running application - 1](etc/1.png?raw=true)

![Screenshot from running application - 2](etc/2.png?raw=true)

회의실을 예약할 수 있는 플랫폼 제작

**주요 적용 기술**
- Java 1.8
- Spring boot
- Spring Security (Oauth2를 사용한 소셜 유저 등록 및 JWT기반 인증)
- Build tool(Gradle)

**회의실 예약 시 제한 사항**
- 10:00 ~ 18:00 시간 제한
- 30분 단위로만 예약 가능
- 해당 회의실, 날짜에 대해 1회 예약
- 희의실 최소 최대 인원 제약
- 시간 중복 허용X

**회의실 취소 시 제한 사항**
- 예약자 혹은 ADMIN 권한 유저만 취소 가능
 
구현 예정
- 유저 그룹 등록, 회의실 예약 시 푸시 알림 및 구글 캘린더 표시

**구현 시 참고**
1. 스프링 시큐리티
- https://www.youtube.com/watch?v=SMZm2aqI_dQ
- https://github.com/szerhusenBC/jwt-spring-security-demo 