SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE reservation_waiting RESTART IDENTITY;
TRUNCATE TABLE reservation_time RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
TRUNCATE TABLE users RESTART IDENTITY; -- 💡 유저 테이블 초기화 추가
SET REFERENTIAL_INTEGRITY TRUE;

-- ==========================================
-- 🧑‍🤝‍🧑 테스트용 유저 데이터 구성 (아이디 정책 반영)
-- ==========================================
INSERT INTO users (login_id, password, name, role) VALUES ('usera', 'password123', '유저A', 'USER');
INSERT INTO users (login_id, password, name, role) VALUES ('userb', 'password123', '유저B', 'USER');
INSERT INTO users (login_id, password, name, role) VALUES ('userc', 'password123', '유저C', 'USER');
INSERT INTO users (login_id, password, name, role) VALUES ('userd', 'password123', '유저D', 'USER');
INSERT INTO users (login_id, password, name, role) VALUES ('usere', 'password123', '유저E', 'USER');
INSERT INTO users (login_id, password, name, role) VALUES ('userf', 'password123', '유저F', 'USER');
INSERT INTO users (login_id, password, name, role) VALUES ('userg', 'password123', '유저G', 'USER');

-- theme (11개)
INSERT INTO theme (name, thumbnail_url, description)
VALUES ('공포의 저택', 'https://picsum.photos/seed/horror/400/300', '어둠 속에 숨겨진 공포를 체험하세요'),
       ('우주 탐험대', 'https://picsum.photos/seed/space/400/300', '은하계를 누비는 우주 탐험'),
       ('탐정 사무소', 'https://picsum.photos/seed/detective/400/300', '단서를 모아 사건을 해결하라'),
       ('마법사의 탑', 'https://picsum.photos/seed/magic/400/300', '마법이 살아 숨쉬는 신비의 탑'),
       ('해적선', 'https://picsum.photos/seed/pirate/400/300', '보물을 찾아 망망대해를 항해'),
       ('고대 신전', 'https://picsum.photos/seed/temple/400/300', '잊혀진 문명의 비밀을 파헤쳐라'),
       ('좀비 연구소', 'https://picsum.photos/seed/zombie/400/300', '바이러스 확산을 막아라'),
       ('타임머신', 'https://picsum.photos/seed/time/400/300', '과거와 미래를 넘나드는 시간 여행'),
       ('사막의 오아시스', 'https://picsum.photos/seed/desert/400/300', '사막 한가운데 숨겨진 비밀'),
       ('폐광', 'https://picsum.photos/seed/mine/400/300', '버려진 광산 속 미스터리'),
       ('유령 호텔', 'https://picsum.photos/seed/ghost/400/300', '체크아웃할 수 없는 호텔');

-- reservation_time
INSERT INTO reservation_time (start_at)
VALUES ('10:00'),
       ('14:00');

-- reservation (집계 기간: 04-28 ~ 05-04)
-- 💡 언더바를 제거한 login_id 매핑으로 변경 완료! (날짜/타임/테마 유니크 조합 슬롯 중복 없음 확인 완료)
INSERT INTO reservation (name, date, time_id, theme_id)
VALUES
-- 테마1: 7건
('usera', '2026-04-28', 1, 1),
('userb', '2026-04-29', 1, 1),
('userc', '2026-04-30', 1, 1),
('userd', '2026-05-01', 1, 1),
('usere', '2026-05-02', 1, 1),
('userf', '2026-05-03', 1, 1),
('userg', '2026-05-04', 1, 1),
-- 테마2: 6건
('usera', '2026-04-28', 2, 2),
('userb', '2026-04-29', 2, 2),
('userc', '2026-04-30', 2, 2),
('userd', '2026-05-01', 2, 2),
('usere', '2026-05-02', 2, 2),
('userf', '2026-05-03', 2, 2),
-- 테마3: 5건
('usera', '2026-04-28', 1, 3),
('userb', '2026-04-30', 1, 3),
('userc', '2026-05-01', 1, 3),
('userd', '2026-05-03', 1, 3),
('usere', '2026-05-04', 1, 3),
-- 테마4: 4건
('usera', '2026-04-29', 2, 4),
('userb', '2026-05-01', 2, 4),
('userc', '2026-05-03', 2, 4),
('userd', '2026-05-04', 2, 4),
-- 테마5: 4건
('usera', '2026-04-28', 1, 5),
('userb', '2026-04-30', 1, 5),
('userc', '2026-05-02', 1, 5),
('userd', '2026-05-04', 1, 5),
-- 테마6: 3건
('usera', '2026-04-29', 2, 6),
('userb', '2026-05-02', 2, 6),
('userc', '2026-05-04', 2, 6),
-- 테마7: 3건
('usera', '2026-04-28', 1, 7),
('userb', '2026-05-01', 1, 7),
('userc', '2026-05-03', 1, 7),
-- 테마8: 2건
('usera', '2026-04-30', 2, 8),
('userb', '2026-05-02', 2, 8),
-- 테마9: 2건
('usera', '2026-05-01', 1, 9),
('userb', '2026-05-04', 1, 9),
-- 테마10: 1건
('usera', '2026-05-03', 2, 10),
-- 테마11: 0건 (집계 기간 외 데이터)
('usera', '2026-04-27', 1, 11),
('userb', '2026-05-05', 1, 11);
