SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE reservation RESTART IDENTITY;
TRUNCATE TABLE reservation_waiting RESTART IDENTITY;
TRUNCATE TABLE reservation_time RESTART IDENTITY;
TRUNCATE TABLE theme RESTART IDENTITY;
TRUNCATE TABLE users RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;

-- ==========================================
-- 🧑‍🤝‍🧑 테스트용 유저 데이터 구성
-- ==========================================
-- adminId는 규칙을 만족합니다. (영문 시작 + 4자 이상)
INSERT INTO users (login_id, password, name, role)
VALUES ('adminId', 'adminpw123', '관리자네오', 'ADMIN');

INSERT INTO users (login_id, password, name, role) VALUES ('usera', 'password123', '유저A', 'USER');
INSERT INTO users (login_id, password, name, role) VALUES ('userb', 'password123', '유저B', 'USER');
INSERT INTO users (login_id, password, name, role) VALUES ('userc', 'password123', '유저C', 'USER');
INSERT INTO users (login_id, password, name, role) VALUES ('userd', 'password123', '유저D', 'USER');
INSERT INTO users (login_id, password, name, role) VALUES ('usere', 'password123', '유저E', 'USER');
INSERT INTO users (login_id, password, name, role) VALUES ('userf', 'password123', '유저F', 'USER');

-- ==========================================
-- 🎪 테마 및 시간 데이터
-- ==========================================
INSERT INTO theme (name, thumbnail_url, description)
VALUES ('공포의 저택', 'https://picsum.photos/seed/horror/400/300', '어둠 속에 숨겨진 공포를 체험하세요');

INSERT INTO theme (name, thumbnail_url, description)
VALUES ('예약없는테마', 'https://picsum.photos/seed/empty/400/300', '예약이 없는 테마');

INSERT INTO reservation_time (start_at) VALUES ('10:00');
INSERT INTO reservation_time (start_at) VALUES ('12:00');
INSERT INTO reservation_time (start_at) VALUES ('13:00');

-- ==========================================
-- 📅 예약 데이터 (상단의 login_id 언더바 제거 반영)
-- ==========================================
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('usera', '2026-04-28', 1, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('userb', '2026-06-05', 2, 1);
INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('userc', '2026-06-05', 1, 1);

-- ==========================================
-- ⏳ 예약 대기 데이터
-- ==========================================
-- 1등: usere (id=1)
INSERT INTO reservation_waiting (name, date, time_id, theme_id, created_at)
VALUES ('usere', '2026-06-05', 1, 1, '2026-06-01 09:00:00');

-- 2등: userb (id=2) -> 취소 대상
INSERT INTO reservation_waiting (name, date, time_id, theme_id, created_at)
VALUES ('userb', '2026-06-05', 1, 1, '2026-06-01 10:00:00');

-- 3등: userf (id=3) -> 순번 당겨짐 확인 대상
INSERT INTO reservation_waiting (name, date, time_id, theme_id, created_at)
VALUES ('userf', '2026-06-05', 1, 1, '2026-06-01 11:00:00');
