-- Demo staff accounts (shown on the login page). Passwords: Admin@123 / Desk@123 — BCrypt, cost 12.
-- Members are library records, not users: they never log in.
INSERT INTO users (name, email, password_hash, role) VALUES
    ('Meenakshi Sundaram', 'admin@folio.dev', '$2a$12$CYY2zDdZ0nvJ3H6Z5d9Vhey9mz29UxZ.zdj4bPZxUtVTYN2SL715G', 'ADMIN'),
    ('Karthik Raja',       'desk@folio.dev',  '$2a$12$rVSx/MDR18ZTbOiPKXsawu3e.UlhxWrAkmeVskvlZQsowMBGsztVi', 'LIBRARIAN');
