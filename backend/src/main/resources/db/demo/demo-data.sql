-- Demo catalogue, members and 60 days of circulation, generated so that every stock change in the ledger
-- adds up to the current book quantities. Loaded only into an empty database (see DemoDataLoader).
DO $$
DECLARE
    v_admin bigint := (SELECT id FROM users WHERE email = 'admin@folio.dev');
    v_desk  bigint := (SELECT id FROM users WHERE email = 'desk@folio.dev');
    v_book_9780143039655 bigint;
    v_book_9780143039648 bigint;
    v_book_9780226568317 bigint;
    v_book_9780812979657 bigint;
    v_book_9781524733155 bigint;
    v_book_9780618711666 bigint;
    v_book_9780312428594 bigint;
    v_book_9780618329960 bigint;
    v_book_9789381020258 bigint;
    v_book_9780802128805 bigint;
    v_book_9780143429012 bigint;
    v_book_9788173711466 bigint;
    v_book_9780143029823 bigint;
    v_book_9780060786526 bigint;
    v_book_9780812976533 bigint;
    v_book_9780618485222 bigint;
    v_book_9780395927205 bigint;
    v_book_9781400030651 bigint;
    v_book_9781416562603 bigint;
    v_book_9780143333388 bigint;
    v_book_9780143031031 bigint;
    v_book_9780143065883 bigint;
    v_book_9780141439518 bigint;
    v_book_9780451524935 bigint;
    v_book_9780061120084 bigint;
    v_book_9780743273565 bigint;
    v_book_9780143058144 bigint;
    v_book_9780141441146 bigint;
    v_book_9780142437247 bigint;
    v_book_9780060883287 bigint;
    v_book_9780156012195 bigint;
    v_book_9780451526342 bigint;
    v_book_9780385474542 bigint;
    v_book_9780132350884 bigint;
    v_book_9781449373320 bigint;
    v_book_9780134685991 bigint;
    v_book_9780201616224 bigint;
    v_book_9780134757599 bigint;
    v_book_9780201633610 bigint;
    v_book_9780262033848 bigint;
    v_book_9780062316097 bigint;
    v_book_9780593189641 bigint;
    v_book_9780374533557 bigint;
    v_book_9780857197689 bigint;
    v_book_9780525510673 bigint;
    v_book_9780553380163 bigint;
    v_book_9780060731335 bigint;
    v_member_0 bigint;
    v_member_1 bigint;
    v_member_2 bigint;
    v_member_3 bigint;
    v_member_4 bigint;
    v_member_5 bigint;
    v_member_6 bigint;
    v_member_7 bigint;
    v_member_8 bigint;
    v_member_9 bigint;
    v_member_10 bigint;
    v_member_11 bigint;
    v_member_12 bigint;
    v_member_13 bigint;
    v_member_14 bigint;
    v_member_15 bigint;
    v_member_16 bigint;
    v_member_17 bigint;
    v_member_18 bigint;
    v_member_19 bigint;
    v_member_20 bigint;
    v_member_21 bigint;
    v_member_22 bigint;
    v_member_23 bigint;
    v_member_24 bigint;
    v_loan_0 bigint;
    v_loan_1 bigint;
    v_loan_2 bigint;
    v_loan_3 bigint;
    v_loan_4 bigint;
    v_loan_5 bigint;
    v_loan_6 bigint;
    v_loan_7 bigint;
    v_loan_8 bigint;
    v_loan_9 bigint;
    v_loan_10 bigint;
    v_loan_11 bigint;
    v_loan_12 bigint;
    v_loan_13 bigint;
    v_loan_14 bigint;
    v_loan_15 bigint;
    v_loan_16 bigint;
    v_loan_17 bigint;
    v_loan_18 bigint;
    v_loan_19 bigint;
    v_loan_20 bigint;
    v_loan_21 bigint;
    v_loan_22 bigint;
    v_loan_23 bigint;
    v_loan_24 bigint;
    v_loan_25 bigint;
    v_loan_26 bigint;
    v_loan_27 bigint;
    v_loan_28 bigint;
    v_loan_29 bigint;
    v_loan_30 bigint;
    v_loan_31 bigint;
    v_loan_32 bigint;
    v_loan_33 bigint;
    v_loan_34 bigint;
    v_loan_35 bigint;
    v_loan_36 bigint;
    v_loan_37 bigint;
    v_loan_38 bigint;
    v_loan_39 bigint;
    v_loan_40 bigint;
    v_loan_41 bigint;
    v_loan_42 bigint;
    v_loan_43 bigint;
    v_loan_44 bigint;
    v_loan_45 bigint;
    v_loan_46 bigint;
    v_loan_47 bigint;
BEGIN
    INSERT INTO genres (name) VALUES ('Biography'), ('Classics'), ('History'), ('Indian Literature'), ('Non-fiction'), ('Science'), ('Short Stories'), ('Tamil Classics'), ('Technology') ON CONFLICT DO NOTHING;
    INSERT INTO publishers (name) VALUES ('Addison-Wesley'), ('Alfred A. Knopf'), ('Anchor Books'), ('Avery'), ('Bantam Books'), ('Farrar, Straus and Giroux'), ('Free Press'), ('Grove Press'), ('HMH Books'), ('Harper'), ('Harper Perennial'), ('Harriman House'), ('Houghton Mifflin'), ('Mariner Books'), ('Nakkheeran Publications'), ('O''Reilly Media'), ('Penguin Books'), ('Penguin Books India'), ('Penguin Classics'), ('Picador'), ('Prentice Hall'), ('Random House'), ('Random House Trade Paperbacks'), ('Scribner'), ('Signet Classics'), ('The MIT Press'), ('Universities Press'), ('University of Chicago Press'), ('Vintage International') ON CONFLICT DO NOTHING;
    INSERT INTO authors (name) VALUES ('R. K. Narayan'), ('Arundhati Roy'), ('Amitav Ghosh'), ('Kalki Krishnamurthy'), ('Perumal Murugan'), ('A. P. J. Abdul Kalam'), ('Arun Tiwari'), ('Vikram Seth'), ('Salman Rushdie'), ('Jhumpa Lahiri'), ('Rohinton Mistry'), ('Aravind Adiga'), ('Ruskin Bond'), ('Jawaharlal Nehru'), ('Khushwant Singh'), ('Jane Austen'), ('George Orwell'), ('Harper Lee'), ('F. Scott Fitzgerald'), ('Fyodor Dostoevsky'), ('Charlotte Brontë'), ('Herman Melville'), ('Gabriel García Márquez'), ('Antoine de Saint-Exupéry'), ('Chinua Achebe'), ('Robert C. Martin'), ('Martin Kleppmann'), ('Joshua Bloch'), ('Andrew Hunt'), ('David Thomas'), ('Martin Fowler'), ('Erich Gamma'), ('Richard Helm'), ('Ralph Johnson'), ('John Vlissides'), ('Thomas H. Cormen'), ('Charles E. Leiserson'), ('Ronald L. Rivest'), ('Clifford Stein'), ('Yuval Noah Harari'), ('James Clear'), ('Daniel Kahneman'), ('Morgan Housel'), ('Tara Westover'), ('Stephen Hawking'), ('Steven D. Levitt'), ('Stephen J. Dubner') ON CONFLICT DO NOTHING;
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780143039655', 'Malgudi Days', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Penguin Books'),
            'en', 2006, 264, 'A-01', 4, 4, ((CURRENT_DATE - 90) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 90) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780143039655;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780143039655, (SELECT id FROM authors WHERE name = 'R. K. Narayan'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780143039648', 'The Guide', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Penguin Books'),
            'en', 2006, 196, 'A-01', 3, 3, ((CURRENT_DATE - 90) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 90) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780143039648;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780143039648, (SELECT id FROM authors WHERE name = 'R. K. Narayan'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780226568317', 'Swami and Friends', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'University of Chicago Press'),
            'en', 1980, 184, 'A-01', 2, 0, ((CURRENT_DATE - 90) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 90) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780226568317;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780226568317, (SELECT id FROM authors WHERE name = 'R. K. Narayan'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780812979657', 'The God of Small Things', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Random House Trade Paperbacks'),
            'en', 2008, 333, 'A-02', 3, 2, ((CURRENT_DATE - 88) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 88) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780812979657;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780812979657, (SELECT id FROM authors WHERE name = 'Arundhati Roy'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9781524733155', 'The Ministry of Utmost Happiness', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Alfred A. Knopf'),
            'en', 2017, 449, 'A-02', 2, 2, ((CURRENT_DATE - 88) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 88) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9781524733155;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9781524733155, (SELECT id FROM authors WHERE name = 'Arundhati Roy'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780618711666', 'The Hungry Tide', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Mariner Books'),
            'en', 2006, 352, 'A-03', 2, 2, ((CURRENT_DATE - 86) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 86) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780618711666;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780618711666, (SELECT id FROM authors WHERE name = 'Amitav Ghosh'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780312428594', 'Sea of Poppies', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Picador'),
            'en', 2009, 543, 'A-03', 3, 3, ((CURRENT_DATE - 86) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 86) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780312428594;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780312428594, (SELECT id FROM authors WHERE name = 'Amitav Ghosh'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780618329960', 'The Shadow Lines', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Houghton Mifflin'),
            'en', 2005, 246, 'A-03', 1, 0, ((CURRENT_DATE - 86) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 86) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780618329960;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780618329960, (SELECT id FROM authors WHERE name = 'Amitav Ghosh'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9789381020258', 'Ponniyin Selvan', NULL, (SELECT id FROM genres WHERE name = 'Tamil Classics'), (SELECT id FROM publishers WHERE name = 'Nakkheeran Publications'),
            'ta', 2013, 2400, 'T-01', 3, 2, ((CURRENT_DATE - 84) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 84) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9789381020258;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9789381020258, (SELECT id FROM authors WHERE name = 'Kalki Krishnamurthy'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780802128805', 'One Part Woman', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Grove Press'),
            'en', 2018, 256, 'A-04', 2, 2, ((CURRENT_DATE - 84) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 84) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780802128805;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780802128805, (SELECT id FROM authors WHERE name = 'Perumal Murugan'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780143429012', 'Pyre', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Penguin Books India'),
            'en', 2017, 224, 'A-04', 2, 1, ((CURRENT_DATE - 84) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 84) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780143429012;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780143429012, (SELECT id FROM authors WHERE name = 'Perumal Murugan'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9788173711466', 'Wings of Fire', 'An Autobiography', (SELECT id FROM genres WHERE name = 'Biography'), (SELECT id FROM publishers WHERE name = 'Universities Press'),
            'en', 2002, 222, 'B-01', 7, 6, ((CURRENT_DATE - 92) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 92) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9788173711466;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9788173711466, (SELECT id FROM authors WHERE name = 'A. P. J. Abdul Kalam'), 1);
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9788173711466, (SELECT id FROM authors WHERE name = 'Arun Tiwari'), 2);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780143029823', 'Ignited Minds', 'Unleashing the Power Within India', (SELECT id FROM genres WHERE name = 'Biography'), (SELECT id FROM publishers WHERE name = 'Penguin Books India'),
            'en', 2003, 205, 'B-01', 3, 3, ((CURRENT_DATE - 92) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 92) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780143029823;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780143029823, (SELECT id FROM authors WHERE name = 'A. P. J. Abdul Kalam'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780060786526', 'A Suitable Boy', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Harper Perennial'),
            'en', 2005, 1474, 'A-05', 1, 1, ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780060786526;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780060786526, (SELECT id FROM authors WHERE name = 'Vikram Seth'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780812976533', 'Midnight''s Children', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Random House Trade Paperbacks'),
            'en', 2006, 560, 'A-05', 2, 2, ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780812976533;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780812976533, (SELECT id FROM authors WHERE name = 'Salman Rushdie'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780618485222', 'The Namesake', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Houghton Mifflin'),
            'en', 2004, 291, 'A-06', 2, 2, ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780618485222;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780618485222, (SELECT id FROM authors WHERE name = 'Jhumpa Lahiri'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780395927205', 'Interpreter of Maladies', NULL, (SELECT id FROM genres WHERE name = 'Short Stories'), (SELECT id FROM publishers WHERE name = 'Mariner Books'),
            'en', 1999, 198, 'A-06', 2, 2, ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780395927205;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780395927205, (SELECT id FROM authors WHERE name = 'Jhumpa Lahiri'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9781400030651', 'A Fine Balance', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Vintage International'),
            'en', 2001, 603, 'A-07', 2, 2, ((CURRENT_DATE - 78) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 78) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9781400030651;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9781400030651, (SELECT id FROM authors WHERE name = 'Rohinton Mistry'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9781416562603', 'The White Tiger', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Free Press'),
            'en', 2008, 276, 'A-07', 3, 3, ((CURRENT_DATE - 78) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 78) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9781416562603;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9781416562603, (SELECT id FROM authors WHERE name = 'Aravind Adiga'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780143333388', 'The Room on the Roof', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Penguin Books India'),
            'en', 2014, 208, 'A-08', 2, 2, ((CURRENT_DATE - 78) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 78) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780143333388;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780143333388, (SELECT id FROM authors WHERE name = 'Ruskin Bond'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780143031031', 'The Discovery of India', NULL, (SELECT id FROM genres WHERE name = 'History'), (SELECT id FROM publishers WHERE name = 'Penguin Books India'),
            'en', 2004, 656, 'H-01', 2, 2, ((CURRENT_DATE - 76) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 76) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780143031031;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780143031031, (SELECT id FROM authors WHERE name = 'Jawaharlal Nehru'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780143065883', 'Train to Pakistan', NULL, (SELECT id FROM genres WHERE name = 'Indian Literature'), (SELECT id FROM publishers WHERE name = 'Penguin Books India'),
            'en', 2009, 181, 'A-08', 1, 1, ((CURRENT_DATE - 76) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 76) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780143065883;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780143065883, (SELECT id FROM authors WHERE name = 'Khushwant Singh'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780141439518', 'Pride and Prejudice', NULL, (SELECT id FROM genres WHERE name = 'Classics'), (SELECT id FROM publishers WHERE name = 'Penguin Books'),
            'en', 2003, 435, 'C-01', 3, 3, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780141439518;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780141439518, (SELECT id FROM authors WHERE name = 'Jane Austen'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780451524935', '1984', NULL, (SELECT id FROM genres WHERE name = 'Classics'), (SELECT id FROM publishers WHERE name = 'Signet Classics'),
            'en', 1977, 328, 'C-01', 3, 2, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780451524935;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780451524935, (SELECT id FROM authors WHERE name = 'George Orwell'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780061120084', 'To Kill a Mockingbird', NULL, (SELECT id FROM genres WHERE name = 'Classics'), (SELECT id FROM publishers WHERE name = 'Harper Perennial'),
            'en', 2006, 323, 'C-02', 3, 2, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780061120084;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780061120084, (SELECT id FROM authors WHERE name = 'Harper Lee'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780743273565', 'The Great Gatsby', NULL, (SELECT id FROM genres WHERE name = 'Classics'), (SELECT id FROM publishers WHERE name = 'Scribner'),
            'en', 2004, 180, 'C-02', 2, 2, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780743273565;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780743273565, (SELECT id FROM authors WHERE name = 'F. Scott Fitzgerald'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780143058144', 'Crime and Punishment', NULL, (SELECT id FROM genres WHERE name = 'Classics'), (SELECT id FROM publishers WHERE name = 'Penguin Classics'),
            'en', 2002, 671, 'C-03', 2, 2, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780143058144;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780143058144, (SELECT id FROM authors WHERE name = 'Fyodor Dostoevsky'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780141441146', 'Jane Eyre', NULL, (SELECT id FROM genres WHERE name = 'Classics'), (SELECT id FROM publishers WHERE name = 'Penguin Books'),
            'en', 2006, 532, 'C-03', 2, 2, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780141441146;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780141441146, (SELECT id FROM authors WHERE name = 'Charlotte Brontë'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780142437247', 'Moby-Dick', 'or, The Whale', (SELECT id FROM genres WHERE name = 'Classics'), (SELECT id FROM publishers WHERE name = 'Penguin Books'),
            'en', 2003, 654, 'C-04', 1, 0, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780142437247;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780142437247, (SELECT id FROM authors WHERE name = 'Herman Melville'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780060883287', 'One Hundred Years of Solitude', NULL, (SELECT id FROM genres WHERE name = 'Classics'), (SELECT id FROM publishers WHERE name = 'Harper Perennial'),
            'en', 2006, 417, 'C-04', 2, 2, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780060883287;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780060883287, (SELECT id FROM authors WHERE name = 'Gabriel García Márquez'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780156012195', 'The Little Prince', NULL, (SELECT id FROM genres WHERE name = 'Classics'), (SELECT id FROM publishers WHERE name = 'HMH Books'),
            'en', 2000, 96, 'C-05', 3, 3, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780156012195;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780156012195, (SELECT id FROM authors WHERE name = 'Antoine de Saint-Exupéry'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780451526342', 'Animal Farm', NULL, (SELECT id FROM genres WHERE name = 'Classics'), (SELECT id FROM publishers WHERE name = 'Signet Classics'),
            'en', 1996, 140, 'C-01', 3, 3, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780451526342;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780451526342, (SELECT id FROM authors WHERE name = 'George Orwell'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780385474542', 'Things Fall Apart', NULL, (SELECT id FROM genres WHERE name = 'Classics'), (SELECT id FROM publishers WHERE name = 'Anchor Books'),
            'en', 1994, 209, 'C-05', 2, 2, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780385474542;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780385474542, (SELECT id FROM authors WHERE name = 'Chinua Achebe'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780132350884', 'Clean Code', 'A Handbook of Agile Software Craftsmanship', (SELECT id FROM genres WHERE name = 'Technology'), (SELECT id FROM publishers WHERE name = 'Prentice Hall'),
            'en', 2008, 431, 'T-02', 4, 3, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780132350884;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780132350884, (SELECT id FROM authors WHERE name = 'Robert C. Martin'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9781449373320', 'Designing Data-Intensive Applications', 'The Big Ideas Behind Reliable, Scalable, and Maintainable Systems', (SELECT id FROM genres WHERE name = 'Technology'), (SELECT id FROM publishers WHERE name = 'O''Reilly Media'),
            'en', 2017, 624, 'T-02', 2, 1, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9781449373320;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9781449373320, (SELECT id FROM authors WHERE name = 'Martin Kleppmann'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780134685991', 'Effective Java', NULL, (SELECT id FROM genres WHERE name = 'Technology'), (SELECT id FROM publishers WHERE name = 'Addison-Wesley'),
            'en', 2017, 416, 'T-02', 2, 2, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780134685991;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780134685991, (SELECT id FROM authors WHERE name = 'Joshua Bloch'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780201616224', 'The Pragmatic Programmer', 'From Journeyman to Master', (SELECT id FROM genres WHERE name = 'Technology'), (SELECT id FROM publishers WHERE name = 'Addison-Wesley'),
            'en', 2000, 321, 'T-03', 2, 2, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780201616224;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780201616224, (SELECT id FROM authors WHERE name = 'Andrew Hunt'), 1);
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780201616224, (SELECT id FROM authors WHERE name = 'David Thomas'), 2);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780134757599', 'Refactoring', 'Improving the Design of Existing Code', (SELECT id FROM genres WHERE name = 'Technology'), (SELECT id FROM publishers WHERE name = 'Addison-Wesley'),
            'en', 2018, 448, 'T-03', 1, 0, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780134757599;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780134757599, (SELECT id FROM authors WHERE name = 'Martin Fowler'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780201633610', 'Design Patterns', 'Elements of Reusable Object-Oriented Software', (SELECT id FROM genres WHERE name = 'Technology'), (SELECT id FROM publishers WHERE name = 'Addison-Wesley'),
            'en', 1994, 395, 'T-03', 2, 1, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780201633610;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780201633610, (SELECT id FROM authors WHERE name = 'Erich Gamma'), 1);
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780201633610, (SELECT id FROM authors WHERE name = 'Richard Helm'), 2);
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780201633610, (SELECT id FROM authors WHERE name = 'Ralph Johnson'), 3);
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780201633610, (SELECT id FROM authors WHERE name = 'John Vlissides'), 4);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780262033848', 'Introduction to Algorithms', NULL, (SELECT id FROM genres WHERE name = 'Technology'), (SELECT id FROM publishers WHERE name = 'The MIT Press'),
            'en', 2009, 1292, 'T-04', 2, 1, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780262033848;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780262033848, (SELECT id FROM authors WHERE name = 'Thomas H. Cormen'), 1);
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780262033848, (SELECT id FROM authors WHERE name = 'Charles E. Leiserson'), 2);
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780262033848, (SELECT id FROM authors WHERE name = 'Ronald L. Rivest'), 3);
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780262033848, (SELECT id FROM authors WHERE name = 'Clifford Stein'), 4);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780062316097', 'Sapiens', 'A Brief History of Humankind', (SELECT id FROM genres WHERE name = 'Non-fiction'), (SELECT id FROM publishers WHERE name = 'Harper'),
            'en', 2015, 464, 'N-01', 3, 3, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780062316097;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780062316097, (SELECT id FROM authors WHERE name = 'Yuval Noah Harari'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780593189641', 'Atomic Habits', NULL, (SELECT id FROM genres WHERE name = 'Non-fiction'), (SELECT id FROM publishers WHERE name = 'Avery'),
            'en', 2018, 320, 'N-01', 5, 4, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780593189641;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780593189641, (SELECT id FROM authors WHERE name = 'James Clear'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780374533557', 'Thinking, Fast and Slow', NULL, (SELECT id FROM genres WHERE name = 'Non-fiction'), (SELECT id FROM publishers WHERE name = 'Farrar, Straus and Giroux'),
            'en', 2013, 499, 'N-02', 2, 2, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780374533557;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780374533557, (SELECT id FROM authors WHERE name = 'Daniel Kahneman'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780857197689', 'The Psychology of Money', NULL, (SELECT id FROM genres WHERE name = 'Non-fiction'), (SELECT id FROM publishers WHERE name = 'Harriman House'),
            'en', 2020, 250, 'N-02', 3, 2, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780857197689;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780857197689, (SELECT id FROM authors WHERE name = 'Morgan Housel'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780525510673', 'Educated', 'A Memoir', (SELECT id FROM genres WHERE name = 'Biography'), (SELECT id FROM publishers WHERE name = 'Random House'),
            'en', 2018, 334, 'B-02', 2, 2, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780525510673;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780525510673, (SELECT id FROM authors WHERE name = 'Tara Westover'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780553380163', 'A Brief History of Time', NULL, (SELECT id FROM genres WHERE name = 'Science'), (SELECT id FROM publishers WHERE name = 'Bantam Books'),
            'en', 1998, 212, 'S-01', 2, 2, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780553380163;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780553380163, (SELECT id FROM authors WHERE name = 'Stephen Hawking'), 1);
    INSERT INTO books (isbn13, title, subtitle, genre_id, publisher_id, language_code, published_year, pages, shelf_location,
                       total_quantity, available_quantity, created_at, updated_at)
    VALUES ('9780060731335', 'Freakonomics', NULL, (SELECT id FROM genres WHERE name = 'Non-fiction'), (SELECT id FROM publishers WHERE name = 'Harper Perennial'),
            'en', 2009, 315, 'N-03', 1, 0, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_book_9780060731335;
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780060731335, (SELECT id FROM authors WHERE name = 'Steven D. Levitt'), 1);
    INSERT INTO book_authors (book_id, author_id, position) VALUES (v_book_9780060731335, (SELECT id FROM authors WHERE name = 'Stephen J. Dubner'), 2);
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 500))::int || '-' || lpad('1', 4, '0'), 'Malathi Duraisamy', 'malathi.d@example.com', '9787345672', '30 Vilakkuthoon', 'Madurai', '625001',
            'INACTIVE', (CURRENT_DATE - 500), ((CURRENT_DATE - 500) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 500) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_23;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 420))::int || '-' || lpad('2', 4, '0'), 'Priya Raman', 'priya.raman@example.com', '9843012345', '14 East Car Street', 'Ramanathapuram', '623501',
            'ACTIVE', (CURRENT_DATE - 420), ((CURRENT_DATE - 420) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 420) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_0;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 400))::int || '-' || lpad('3', 4, '0'), 'Arun Kumar Velu', 'arun.velu@example.com', '9894123456', '7 Salai Road', 'Ramanathapuram', '623501',
            'ACTIVE', (CURRENT_DATE - 400), ((CURRENT_DATE - 400) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 400) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_1;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 380))::int || '-' || lpad('4', 4, '0'), 'Meena Sundaresan', 'meena.s@example.com', '9790234567', '22 Bharathi Nagar', 'Ramanathapuram', '623503',
            'ACTIVE', (CURRENT_DATE - 380), ((CURRENT_DATE - 380) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 380) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_2;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 360))::int || '-' || lpad('5', 4, '0'), 'Karthikeyan Murugesan', 'karthik.m@example.com', '9677345678', '3 Kenikarai Main Road', 'Ramanathapuram', '623501',
            'ACTIVE', (CURRENT_DATE - 360), ((CURRENT_DATE - 360) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 360) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_3;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 350))::int || '-' || lpad('6', 4, '0'), 'Lakshmi Narayanan', 'lakshmi.n@example.com', '9443456789', '18 Pasumpon Nagar', 'Ramanathapuram', '623504',
            'ACTIVE', (CURRENT_DATE - 350), ((CURRENT_DATE - 350) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 350) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_4;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 340))::int || '-' || lpad('7', 4, '0'), 'Divya Balasubramanian', 'divya.bala@example.com', '9789567890', '9 Anna Nagar 4th Street', 'Madurai', '625020',
            'ACTIVE', (CURRENT_DATE - 340), ((CURRENT_DATE - 340) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 340) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_5;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 330))::int || '-' || lpad('8', 4, '0'), 'Senthil Nathan', 'senthil.nathan@example.com', '9842678901', '41 Town Hall Road', 'Madurai', '625001',
            'ACTIVE', (CURRENT_DATE - 330), ((CURRENT_DATE - 330) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 330) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_6;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 320))::int || '-' || lpad('9', 4, '0'), 'Kavitha Rajendran', 'kavitha.r@example.com', '9600789012', '12 KK Nagar Main Road', 'Madurai', '625020',
            'ACTIVE', (CURRENT_DATE - 320), ((CURRENT_DATE - 320) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 320) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_7;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 310))::int || '-' || lpad('10', 4, '0'), 'Rajesh Kannan', 'rajesh.kannan@example.com', '9944456783', '13 Paramakudi Road', 'Ramanathapuram', '623501',
            'SUSPENDED', (CURRENT_DATE - 310), ((CURRENT_DATE - 310) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 310) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_24;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 300))::int || '-' || lpad('11', 4, '0'), 'Muthukumar Pandian', 'muthu.pandian@example.com', '9865890123', '5 Simmakkal', 'Madurai', '625001',
            'ACTIVE', (CURRENT_DATE - 300), ((CURRENT_DATE - 300) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 300) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_8;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 290))::int || '-' || lpad('12', 4, '0'), 'Revathi Chandrasekar', 'revathi.c@example.com', '9751901234', '27 Tallakulam', 'Madurai', '625002',
            'ACTIVE', (CURRENT_DATE - 290), ((CURRENT_DATE - 290) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 290) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_9;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 270))::int || '-' || lpad('13', 4, '0'), 'Vignesh Subramanian', 'vignesh.s@example.com', '9884012345', '3 TTK Road, Alwarpet', 'Chennai', '600018',
            'ACTIVE', (CURRENT_DATE - 270), ((CURRENT_DATE - 270) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 270) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_10;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 260))::int || '-' || lpad('14', 4, '0'), 'Anitha Krishnan', 'anitha.k@example.com', '9940123456', '16 Habibullah Road, T. Nagar', 'Chennai', '600017',
            'ACTIVE', (CURRENT_DATE - 260), ((CURRENT_DATE - 260) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 260) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_11;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 250))::int || '-' || lpad('15', 4, '0'), 'Ganesh Ramasamy', 'ganesh.r@example.com', '9176234567', '8 2nd Avenue, Anna Nagar', 'Chennai', '600040',
            'ACTIVE', (CURRENT_DATE - 250), ((CURRENT_DATE - 250) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 250) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_12;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 240))::int || '-' || lpad('16', 4, '0'), 'Sowmya Venkatesh', 'sowmya.v@example.com', '9003345678', '21 Kutchery Road, Mylapore', 'Chennai', '600004',
            'ACTIVE', (CURRENT_DATE - 240), ((CURRENT_DATE - 240) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 240) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_13;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 220))::int || '-' || lpad('17', 4, '0'), 'Harish Gopalakrishnan', 'harish.g@example.com', '9566456789', '11 Velachery Main Road', 'Chennai', '600042',
            'ACTIVE', (CURRENT_DATE - 220), ((CURRENT_DATE - 220) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 220) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_14;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 200))::int || '-' || lpad('18', 4, '0'), 'Nandhini Sekar', 'nandhini.sekar@example.com', '8754567890', '34 Lake View Road, West Mambalam', 'Chennai', '600033',
            'ACTIVE', (CURRENT_DATE - 200), ((CURRENT_DATE - 200) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 200) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_15;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 180))::int || '-' || lpad('19', 4, '0'), 'Prakash Annamalai', 'prakash.a@example.com', '9345678901', '2 Collectorate Road', 'Ramanathapuram', '623503',
            'ACTIVE', (CURRENT_DATE - 180), ((CURRENT_DATE - 180) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 180) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_16;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 160))::int || '-' || lpad('20', 4, '0'), 'Deepa Thirumalai', 'deepa.t@example.com', '9488789012', '15 Gandhi Nagar', 'Madurai', '625020',
            'ACTIVE', (CURRENT_DATE - 160), ((CURRENT_DATE - 160) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 160) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_17;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 140))::int || '-' || lpad('21', 4, '0'), 'Saravanan Ilango', 'saravanan.i@example.com', '9025890123', '6 Mettu Street', 'Ramanathapuram', '623501',
            'ACTIVE', (CURRENT_DATE - 140), ((CURRENT_DATE - 140) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 140) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_18;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 120))::int || '-' || lpad('22', 4, '0'), 'Janani Mohan', 'janani.mohan@example.com', '8939901234', '19 Besant Avenue, Adyar', 'Chennai', '600020',
            'ACTIVE', (CURRENT_DATE - 120), ((CURRENT_DATE - 120) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 120) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_19;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 100))::int || '-' || lpad('23', 4, '0'), 'Balaji Ravichandran', 'balaji.r@example.com', '9123012345', '25 Bypass Road', 'Madurai', '625010',
            'ACTIVE', (CURRENT_DATE - 100), ((CURRENT_DATE - 100) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 100) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_20;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 80))::int || '-' || lpad('24', 4, '0'), 'Keerthana Pillai', 'keerthana.p@example.com', '9750123450', '4 North Street', 'Ramanathapuram', '623504',
            'ACTIVE', (CURRENT_DATE - 80), ((CURRENT_DATE - 80) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 80) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_21;
    INSERT INTO members (membership_number, full_name, email, phone, address_line, city, postal_code, status, joined_on, created_at, updated_at)
    VALUES ('FOL-' || extract(year FROM (CURRENT_DATE - 60))::int || '-' || lpad('25', 4, '0'), 'Ashwin Selvaraj', 'ashwin.s@example.com', '9840234561', '10 Greams Road', 'Chennai', '600006',
            'ACTIVE', (CURRENT_DATE - 60), ((CURRENT_DATE - 60) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata', ((CURRENT_DATE - 60) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata')
    RETURNING id INTO v_member_22;
    PERFORM setval('membership_number_seq', 25);
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780143039655, v_member_0, ((CURRENT_DATE - 58) + TIME '10:00') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 44), ((CURRENT_DATE - 47) + TIME '16:00') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_0;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9788173711466, v_member_1, ((CURRENT_DATE - 57) + TIME '10:01') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 43), ((CURRENT_DATE - 45) + TIME '16:01') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_1;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780451524935, v_member_2, ((CURRENT_DATE - 56) + TIME '10:02') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 42), ((CURRENT_DATE - 44) + TIME '16:02') AT TIME ZONE 'Asia/Kolkata', 0, v_admin, v_admin)
    RETURNING id INTO v_loan_2;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780132350884, v_member_10, ((CURRENT_DATE - 55) + TIME '10:03') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 41), ((CURRENT_DATE - 43) + TIME '16:03') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_3;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780062316097, v_member_11, ((CURRENT_DATE - 54) + TIME '10:04') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 40), ((CURRENT_DATE - 42) + TIME '16:04') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_4;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780593189641, v_member_12, ((CURRENT_DATE - 53) + TIME '10:05') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 39), ((CURRENT_DATE - 41) + TIME '16:05') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_5;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780812979657, v_member_5, ((CURRENT_DATE - 52) + TIME '10:06') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 38), ((CURRENT_DATE - 40) + TIME '16:06') AT TIME ZONE 'Asia/Kolkata', 0, v_admin, v_admin)
    RETURNING id INTO v_loan_6;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780141439518, v_member_13, ((CURRENT_DATE - 51) + TIME '10:07') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 37), ((CURRENT_DATE - 39) + TIME '16:07') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_7;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9781449373320, v_member_14, ((CURRENT_DATE - 50) + TIME '10:08') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 36), ((CURRENT_DATE - 38) + TIME '16:08') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_8;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780061120084, v_member_6, ((CURRENT_DATE - 49) + TIME '10:09') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 35), ((CURRENT_DATE - 37) + TIME '16:09') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_9;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780156012195, v_member_15, ((CURRENT_DATE - 48) + TIME '10:10') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 34), ((CURRENT_DATE - 36) + TIME '16:10') AT TIME ZONE 'Asia/Kolkata', 0, v_admin, v_admin)
    RETURNING id INTO v_loan_10;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780857197689, v_member_16, ((CURRENT_DATE - 47) + TIME '10:11') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 33), ((CURRENT_DATE - 35) + TIME '16:11') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_11;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780312428594, v_member_7, ((CURRENT_DATE - 46) + TIME '10:12') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 32), ((CURRENT_DATE - 34) + TIME '16:12') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_12;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9789381020258, v_member_8, ((CURRENT_DATE - 45) + TIME '10:13') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 31), ((CURRENT_DATE - 33) + TIME '16:13') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_13;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780143039648, v_member_9, ((CURRENT_DATE - 44) + TIME '10:14') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 30), ((CURRENT_DATE - 32) + TIME '16:14') AT TIME ZONE 'Asia/Kolkata', 0, v_admin, v_admin)
    RETURNING id INTO v_loan_14;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780134685991, v_member_19, ((CURRENT_DATE - 43) + TIME '10:15') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 29), ((CURRENT_DATE - 31) + TIME '16:15') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_15;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780593189641, v_member_20, ((CURRENT_DATE - 40) + TIME '10:16') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 26), ((CURRENT_DATE - 28) + TIME '16:16') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_16;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780451526342, v_member_17, ((CURRENT_DATE - 38) + TIME '10:17') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 24), ((CURRENT_DATE - 26) + TIME '16:17') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_17;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9781416562603, v_member_18, ((CURRENT_DATE - 36) + TIME '10:18') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 22), ((CURRENT_DATE - 24) + TIME '16:18') AT TIME ZONE 'Asia/Kolkata', 0, v_admin, v_admin)
    RETURNING id INTO v_loan_18;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9788173711466, v_member_21, ((CURRENT_DATE - 34) + TIME '10:19') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 20), ((CURRENT_DATE - 22) + TIME '16:19') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_19;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780060786526, v_member_3, ((CURRENT_DATE - 50) + TIME '10:20') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 22), ((CURRENT_DATE - 25) + TIME '16:20') AT TIME ZONE 'Asia/Kolkata', 1, v_desk, v_desk)
    RETURNING id INTO v_loan_20;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780062316097, v_member_4, ((CURRENT_DATE - 45) + TIME '10:21') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 31), ((CURRENT_DATE - 26) + TIME '16:21') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_21;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780374533557, v_member_6, ((CURRENT_DATE - 42) + TIME '10:22') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 28), ((CURRENT_DATE - 20) + TIME '16:22') AT TIME ZONE 'Asia/Kolkata', 0, v_admin, v_admin)
    RETURNING id INTO v_loan_22;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780618485222, v_member_7, ((CURRENT_DATE - 40) + TIME '10:23') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 26), ((CURRENT_DATE - 23) + TIME '16:23') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_23;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780812976533, v_member_9, ((CURRENT_DATE - 35) + TIME '10:24') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 21), ((CURRENT_DATE - 15) + TIME '16:24') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_24;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780743273565, v_member_22, ((CURRENT_DATE - 33) + TIME '10:25') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 19), ((CURRENT_DATE - 12) + TIME '16:25') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_25;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9781400030651, v_member_24, ((CURRENT_DATE - 48) + TIME '10:26') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 34), ((CURRENT_DATE - 9) + TIME '16:26') AT TIME ZONE 'Asia/Kolkata', 0, v_admin, v_admin)
    RETURNING id INTO v_loan_26;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780141441146, v_member_13, ((CURRENT_DATE - 30) + TIME '10:27') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 16), ((CURRENT_DATE - 10) + TIME '16:27') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_27;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780525510673, v_member_14, ((CURRENT_DATE - 29) + TIME '10:28') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 15), ((CURRENT_DATE - 11) + TIME '16:28') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_28;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780143031031, v_member_16, ((CURRENT_DATE - 28) + TIME '10:29') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 14), ((CURRENT_DATE - 12) + TIME '16:29') AT TIME ZONE 'Asia/Kolkata', 0, v_desk, v_desk)
    RETURNING id INTO v_loan_29;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780593189641, v_member_0, ((CURRENT_DATE - 10) + TIME '10:30') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE + 4), NULL, 0, v_desk, NULL)
    RETURNING id INTO v_loan_30;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780132350884, v_member_0, ((CURRENT_DATE - 8) + TIME '10:31') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE + 6), NULL, 0, v_desk, NULL)
    RETURNING id INTO v_loan_31;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9789381020258, v_member_0, ((CURRENT_DATE - 5) + TIME '10:32') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE + 9), NULL, 0, v_desk, NULL)
    RETURNING id INTO v_loan_32;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780618329960, v_member_1, ((CURRENT_DATE - 14) + TIME '10:33') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 0), NULL, 0, v_desk, NULL)
    RETURNING id INTO v_loan_33;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780451524935, v_member_2, ((CURRENT_DATE - 14) + TIME '10:34') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 0), NULL, 0, v_admin, NULL)
    RETURNING id INTO v_loan_34;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780142437247, v_member_3, ((CURRENT_DATE - 22) + TIME '10:35') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 8), NULL, 0, v_desk, NULL)
    RETURNING id INTO v_loan_35;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780134757599, v_member_4, ((CURRENT_DATE - 19) + TIME '10:36') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 5), NULL, 0, v_desk, NULL)
    RETURNING id INTO v_loan_36;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780060731335, v_member_5, ((CURRENT_DATE - 17) + TIME '10:37') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 3), NULL, 0, v_admin, NULL)
    RETURNING id INTO v_loan_37;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780201633610, v_member_6, ((CURRENT_DATE - 25) + TIME '10:38') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE - 11), NULL, 0, v_desk, NULL)
    RETURNING id INTO v_loan_38;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9781449373320, v_member_10, ((CURRENT_DATE - 20) + TIME '10:39') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE + 8), NULL, 1, v_desk, NULL)
    RETURNING id INTO v_loan_39;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780857197689, v_member_11, ((CURRENT_DATE - 6) + TIME '10:40') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE + 8), NULL, 0, v_desk, NULL)
    RETURNING id INTO v_loan_40;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780061120084, v_member_12, ((CURRENT_DATE - 4) + TIME '10:41') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE + 10), NULL, 0, v_admin, NULL)
    RETURNING id INTO v_loan_41;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780262033848, v_member_15, ((CURRENT_DATE - 9) + TIME '10:42') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE + 5), NULL, 0, v_desk, NULL)
    RETURNING id INTO v_loan_42;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780812979657, v_member_17, ((CURRENT_DATE - 3) + TIME '10:43') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE + 11), NULL, 0, v_desk, NULL)
    RETURNING id INTO v_loan_43;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9788173711466, v_member_19, ((CURRENT_DATE - 2) + TIME '10:44') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE + 12), NULL, 0, v_desk, NULL)
    RETURNING id INTO v_loan_44;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780226568317, v_member_20, ((CURRENT_DATE - 7) + TIME '10:45') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE + 7), NULL, 0, v_desk, NULL)
    RETURNING id INTO v_loan_45;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780226568317, v_member_21, ((CURRENT_DATE - 11) + TIME '10:46') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE + 3), NULL, 0, v_desk, NULL)
    RETURNING id INTO v_loan_46;
    INSERT INTO loans (book_id, member_id, issued_at, due_date, returned_at, renewal_count, issued_by, returned_by)
    VALUES (v_book_9780143429012, v_member_22, ((CURRENT_DATE - 1) + TIME '10:47') AT TIME ZONE 'Asia/Kolkata', (CURRENT_DATE + 13), NULL, 0, v_desk, NULL)
    RETURNING id INTO v_loan_47;
    INSERT INTO fines (loan_id, overdue_days, daily_rate, status, resolved_at, resolved_by, resolution_note, created_at) VALUES
        (v_loan_21, 5, 5.00, 'PAID', ((CURRENT_DATE - 24) + TIME '15:00') AT TIME ZONE 'Asia/Kolkata', v_desk, NULL, ((CURRENT_DATE - 26) + TIME '16:21') AT TIME ZONE 'Asia/Kolkata'),
        (v_loan_22, 8, 5.00, 'PAID', ((CURRENT_DATE - 18) + TIME '15:00') AT TIME ZONE 'Asia/Kolkata', v_admin, NULL, ((CURRENT_DATE - 20) + TIME '16:22') AT TIME ZONE 'Asia/Kolkata'),
        (v_loan_23, 3, 5.00, 'WAIVED', ((CURRENT_DATE - 22) + TIME '15:00') AT TIME ZONE 'Asia/Kolkata', v_admin, 'Member was hospitalised; return delayed for medical reasons', ((CURRENT_DATE - 23) + TIME '16:23') AT TIME ZONE 'Asia/Kolkata'),
        (v_loan_24, 6, 5.00, 'UNPAID', NULL, NULL, NULL, ((CURRENT_DATE - 15) + TIME '16:24') AT TIME ZONE 'Asia/Kolkata'),
        (v_loan_25, 7, 5.00, 'UNPAID', NULL, NULL, NULL, ((CURRENT_DATE - 12) + TIME '16:25') AT TIME ZONE 'Asia/Kolkata'),
        (v_loan_26, 25, 5.00, 'UNPAID', NULL, NULL, NULL, ((CURRENT_DATE - 9) + TIME '16:26') AT TIME ZONE 'Asia/Kolkata'),
        (v_loan_27, 6, 5.00, 'PAID', ((CURRENT_DATE - 8) + TIME '15:00') AT TIME ZONE 'Asia/Kolkata', v_desk, NULL, ((CURRENT_DATE - 10) + TIME '16:27') AT TIME ZONE 'Asia/Kolkata'),
        (v_loan_28, 4, 5.00, 'UNPAID', NULL, NULL, NULL, ((CURRENT_DATE - 11) + TIME '16:28') AT TIME ZONE 'Asia/Kolkata'),
        (v_loan_29, 2, 5.00, 'PAID', ((CURRENT_DATE - 10) + TIME '15:00') AT TIME ZONE 'Asia/Kolkata', v_desk, NULL, ((CURRENT_DATE - 12) + TIME '16:29') AT TIME ZONE 'Asia/Kolkata');
    INSERT INTO inventory_transactions (book_id, type, quantity_change, total_before, total_after, available_before, available_after, reason, note, loan_id, performed_by, performed_at) VALUES
        (v_book_9788173711466, 'INITIAL_STOCK', 5, 0, 5, 0, 5, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 92) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143029823, 'INITIAL_STOCK', 3, 0, 3, 0, 3, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 92) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143039655, 'INITIAL_STOCK', 4, 0, 4, 0, 4, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 90) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143039648, 'INITIAL_STOCK', 3, 0, 3, 0, 3, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 90) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780226568317, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 90) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780812979657, 'INITIAL_STOCK', 3, 0, 3, 0, 3, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 88) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9781524733155, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 88) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780618711666, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 86) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780312428594, 'INITIAL_STOCK', 3, 0, 3, 0, 3, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 86) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780618329960, 'INITIAL_STOCK', 1, 0, 1, 0, 1, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 86) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780141439518, 'INITIAL_STOCK', 3, 0, 3, 0, 3, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780451524935, 'INITIAL_STOCK', 4, 0, 4, 0, 4, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780061120084, 'INITIAL_STOCK', 3, 0, 3, 0, 3, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780743273565, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143058144, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780141441146, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780142437247, 'INITIAL_STOCK', 1, 0, 1, 0, 1, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780060883287, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780156012195, 'INITIAL_STOCK', 3, 0, 3, 0, 3, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780451526342, 'INITIAL_STOCK', 3, 0, 3, 0, 3, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780385474542, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9789381020258, 'INITIAL_STOCK', 3, 0, 3, 0, 3, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 84) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780802128805, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 84) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143429012, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 84) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780060786526, 'INITIAL_STOCK', 1, 0, 1, 0, 1, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780812976533, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780618485222, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780395927205, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9781400030651, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 78) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9781416562603, 'INITIAL_STOCK', 3, 0, 3, 0, 3, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 78) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143333388, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 78) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143031031, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 76) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143065883, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 76) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780132350884, 'INITIAL_STOCK', 3, 0, 3, 0, 3, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9781449373320, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780134685991, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780201616224, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780134757599, 'INITIAL_STOCK', 1, 0, 1, 0, 1, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780201633610, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780262033848, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780062316097, 'INITIAL_STOCK', 3, 0, 3, 0, 3, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780593189641, 'INITIAL_STOCK', 4, 0, 4, 0, 4, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780374533557, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780857197689, 'INITIAL_STOCK', 3, 0, 3, 0, 3, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780525510673, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780553380163, 'INITIAL_STOCK', 2, 0, 2, 0, 2, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780060731335, 'INITIAL_STOCK', 1, 0, 1, 0, 1, 'Initial stock', NULL, NULL, v_admin, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9788173711466, 'STOCK_IN', 2, 5, 7, 5, 7, 'Purchase', 'Second order from Higginbothams', NULL, v_admin, ((CURRENT_DATE - 60) + TIME '11:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143039655, 'ISSUE', -1, 4, 4, 4, 3, NULL, NULL, v_loan_0, v_desk, ((CURRENT_DATE - 58) + TIME '10:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9788173711466, 'ISSUE', -1, 7, 7, 7, 6, NULL, NULL, v_loan_1, v_desk, ((CURRENT_DATE - 57) + TIME '10:01') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780451524935, 'ISSUE', -1, 4, 4, 4, 3, NULL, NULL, v_loan_2, v_admin, ((CURRENT_DATE - 56) + TIME '10:02') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780132350884, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_3, v_desk, ((CURRENT_DATE - 55) + TIME '10:03') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780062316097, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_4, v_desk, ((CURRENT_DATE - 54) + TIME '10:04') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780593189641, 'ISSUE', -1, 4, 4, 4, 3, NULL, NULL, v_loan_5, v_desk, ((CURRENT_DATE - 53) + TIME '10:05') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780812979657, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_6, v_admin, ((CURRENT_DATE - 52) + TIME '10:06') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780141439518, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_7, v_desk, ((CURRENT_DATE - 51) + TIME '10:07') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9781449373320, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_8, v_desk, ((CURRENT_DATE - 50) + TIME '10:08') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780060786526, 'ISSUE', -1, 1, 1, 1, 0, NULL, NULL, v_loan_20, v_desk, ((CURRENT_DATE - 50) + TIME '10:20') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780593189641, 'STOCK_IN', 1, 4, 5, 3, 4, 'Donation', 'Donated by a member', NULL, v_desk, ((CURRENT_DATE - 50) + TIME '11:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780061120084, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_9, v_desk, ((CURRENT_DATE - 49) + TIME '10:09') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780156012195, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_10, v_admin, ((CURRENT_DATE - 48) + TIME '10:10') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9781400030651, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_26, v_admin, ((CURRENT_DATE - 48) + TIME '10:26') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780857197689, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_11, v_desk, ((CURRENT_DATE - 47) + TIME '10:11') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143039655, 'RETURN', 1, 4, 4, 3, 4, NULL, NULL, v_loan_0, v_desk, ((CURRENT_DATE - 47) + TIME '16:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780312428594, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_12, v_desk, ((CURRENT_DATE - 46) + TIME '10:12') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9789381020258, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_13, v_desk, ((CURRENT_DATE - 45) + TIME '10:13') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780062316097, 'ISSUE', -1, 3, 3, 2, 1, NULL, NULL, v_loan_21, v_desk, ((CURRENT_DATE - 45) + TIME '10:21') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9788173711466, 'RETURN', 1, 7, 7, 6, 7, NULL, NULL, v_loan_1, v_desk, ((CURRENT_DATE - 45) + TIME '16:01') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143039648, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_14, v_admin, ((CURRENT_DATE - 44) + TIME '10:14') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780451524935, 'RETURN', 1, 4, 4, 3, 4, NULL, NULL, v_loan_2, v_admin, ((CURRENT_DATE - 44) + TIME '16:02') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780134685991, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_15, v_desk, ((CURRENT_DATE - 43) + TIME '10:15') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780132350884, 'RETURN', 1, 3, 3, 2, 3, NULL, NULL, v_loan_3, v_desk, ((CURRENT_DATE - 43) + TIME '16:03') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780374533557, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_22, v_admin, ((CURRENT_DATE - 42) + TIME '10:22') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780062316097, 'RETURN', 1, 3, 3, 1, 2, NULL, NULL, v_loan_4, v_desk, ((CURRENT_DATE - 42) + TIME '16:04') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780593189641, 'RETURN', 1, 5, 5, 4, 5, NULL, NULL, v_loan_5, v_desk, ((CURRENT_DATE - 41) + TIME '16:05') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780593189641, 'ISSUE', -1, 5, 5, 5, 4, NULL, NULL, v_loan_16, v_desk, ((CURRENT_DATE - 40) + TIME '10:16') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780618485222, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_23, v_desk, ((CURRENT_DATE - 40) + TIME '10:23') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780812979657, 'RETURN', 1, 3, 3, 2, 3, NULL, NULL, v_loan_6, v_admin, ((CURRENT_DATE - 40) + TIME '16:06') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780141439518, 'RETURN', 1, 3, 3, 2, 3, NULL, NULL, v_loan_7, v_desk, ((CURRENT_DATE - 39) + TIME '16:07') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780451526342, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_17, v_desk, ((CURRENT_DATE - 38) + TIME '10:17') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9781449373320, 'RETURN', 1, 2, 2, 1, 2, NULL, NULL, v_loan_8, v_desk, ((CURRENT_DATE - 38) + TIME '16:08') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780061120084, 'RETURN', 1, 3, 3, 2, 3, NULL, NULL, v_loan_9, v_desk, ((CURRENT_DATE - 37) + TIME '16:09') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9781416562603, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_18, v_admin, ((CURRENT_DATE - 36) + TIME '10:18') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780156012195, 'RETURN', 1, 3, 3, 2, 3, NULL, NULL, v_loan_10, v_admin, ((CURRENT_DATE - 36) + TIME '16:10') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780812976533, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_24, v_desk, ((CURRENT_DATE - 35) + TIME '10:24') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780857197689, 'RETURN', 1, 3, 3, 2, 3, NULL, NULL, v_loan_11, v_desk, ((CURRENT_DATE - 35) + TIME '16:11') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9788173711466, 'ISSUE', -1, 7, 7, 7, 6, NULL, NULL, v_loan_19, v_desk, ((CURRENT_DATE - 34) + TIME '10:19') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780312428594, 'RETURN', 1, 3, 3, 2, 3, NULL, NULL, v_loan_12, v_desk, ((CURRENT_DATE - 34) + TIME '16:12') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780743273565, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_25, v_desk, ((CURRENT_DATE - 33) + TIME '10:25') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9789381020258, 'RETURN', 1, 3, 3, 2, 3, NULL, NULL, v_loan_13, v_desk, ((CURRENT_DATE - 33) + TIME '16:13') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143039648, 'RETURN', 1, 3, 3, 2, 3, NULL, NULL, v_loan_14, v_admin, ((CURRENT_DATE - 32) + TIME '16:14') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780134685991, 'RETURN', 1, 2, 2, 1, 2, NULL, NULL, v_loan_15, v_desk, ((CURRENT_DATE - 31) + TIME '16:15') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780141441146, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_27, v_desk, ((CURRENT_DATE - 30) + TIME '10:27') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780451524935, 'STOCK_OUT', -1, 4, 3, 4, 3, 'DAMAGED', 'Water damage in the reading room', NULL, v_admin, ((CURRENT_DATE - 30) + TIME '11:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780525510673, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_28, v_desk, ((CURRENT_DATE - 29) + TIME '10:28') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143031031, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_29, v_desk, ((CURRENT_DATE - 28) + TIME '10:29') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780593189641, 'RETURN', 1, 5, 5, 4, 5, NULL, NULL, v_loan_16, v_desk, ((CURRENT_DATE - 28) + TIME '16:16') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780451526342, 'RETURN', 1, 3, 3, 2, 3, NULL, NULL, v_loan_17, v_desk, ((CURRENT_DATE - 26) + TIME '16:17') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780062316097, 'RETURN', 1, 3, 3, 2, 3, NULL, NULL, v_loan_21, v_desk, ((CURRENT_DATE - 26) + TIME '16:21') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780201633610, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_38, v_desk, ((CURRENT_DATE - 25) + TIME '10:38') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780132350884, 'STOCK_IN', 1, 3, 4, 3, 4, 'Purchase', NULL, NULL, v_admin, ((CURRENT_DATE - 25) + TIME '11:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780060786526, 'RETURN', 1, 1, 1, 0, 1, NULL, NULL, v_loan_20, v_desk, ((CURRENT_DATE - 25) + TIME '16:20') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9781416562603, 'RETURN', 1, 3, 3, 2, 3, NULL, NULL, v_loan_18, v_admin, ((CURRENT_DATE - 24) + TIME '16:18') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780618485222, 'RETURN', 1, 2, 2, 1, 2, NULL, NULL, v_loan_23, v_desk, ((CURRENT_DATE - 23) + TIME '16:23') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780142437247, 'ISSUE', -1, 1, 1, 1, 0, NULL, NULL, v_loan_35, v_desk, ((CURRENT_DATE - 22) + TIME '10:35') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9788173711466, 'RETURN', 1, 7, 7, 6, 7, NULL, NULL, v_loan_19, v_desk, ((CURRENT_DATE - 22) + TIME '16:19') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9781449373320, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_39, v_desk, ((CURRENT_DATE - 20) + TIME '10:39') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780374533557, 'RETURN', 1, 2, 2, 1, 2, NULL, NULL, v_loan_22, v_admin, ((CURRENT_DATE - 20) + TIME '16:22') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780134757599, 'ISSUE', -1, 1, 1, 1, 0, NULL, NULL, v_loan_36, v_desk, ((CURRENT_DATE - 19) + TIME '10:36') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143065883, 'STOCK_OUT', -1, 2, 1, 2, 1, 'LOST', 'Not returned after inter-branch transfer', NULL, v_admin, ((CURRENT_DATE - 18) + TIME '11:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780060731335, 'ISSUE', -1, 1, 1, 1, 0, NULL, NULL, v_loan_37, v_admin, ((CURRENT_DATE - 17) + TIME '10:37') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780812976533, 'RETURN', 1, 2, 2, 1, 2, NULL, NULL, v_loan_24, v_desk, ((CURRENT_DATE - 15) + TIME '16:24') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780618329960, 'ISSUE', -1, 1, 1, 1, 0, NULL, NULL, v_loan_33, v_desk, ((CURRENT_DATE - 14) + TIME '10:33') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780451524935, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_34, v_admin, ((CURRENT_DATE - 14) + TIME '10:34') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780743273565, 'RETURN', 1, 2, 2, 1, 2, NULL, NULL, v_loan_25, v_desk, ((CURRENT_DATE - 12) + TIME '16:25') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143031031, 'RETURN', 1, 2, 2, 1, 2, NULL, NULL, v_loan_29, v_desk, ((CURRENT_DATE - 12) + TIME '16:29') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780226568317, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_46, v_desk, ((CURRENT_DATE - 11) + TIME '10:46') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780525510673, 'RETURN', 1, 2, 2, 1, 2, NULL, NULL, v_loan_28, v_desk, ((CURRENT_DATE - 11) + TIME '16:28') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780593189641, 'ISSUE', -1, 5, 5, 5, 4, NULL, NULL, v_loan_30, v_desk, ((CURRENT_DATE - 10) + TIME '10:30') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780141441146, 'RETURN', 1, 2, 2, 1, 2, NULL, NULL, v_loan_27, v_desk, ((CURRENT_DATE - 10) + TIME '16:27') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780262033848, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_42, v_desk, ((CURRENT_DATE - 9) + TIME '10:42') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9781400030651, 'RETURN', 1, 2, 2, 1, 2, NULL, NULL, v_loan_26, v_admin, ((CURRENT_DATE - 9) + TIME '16:26') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780132350884, 'ISSUE', -1, 4, 4, 4, 3, NULL, NULL, v_loan_31, v_desk, ((CURRENT_DATE - 8) + TIME '10:31') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780226568317, 'ISSUE', -1, 2, 2, 1, 0, NULL, NULL, v_loan_45, v_desk, ((CURRENT_DATE - 7) + TIME '10:45') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780857197689, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_40, v_desk, ((CURRENT_DATE - 6) + TIME '10:40') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9789381020258, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_32, v_desk, ((CURRENT_DATE - 5) + TIME '10:32') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780061120084, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_41, v_admin, ((CURRENT_DATE - 4) + TIME '10:41') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780812979657, 'ISSUE', -1, 3, 3, 3, 2, NULL, NULL, v_loan_43, v_desk, ((CURRENT_DATE - 3) + TIME '10:43') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9788173711466, 'ISSUE', -1, 7, 7, 7, 6, NULL, NULL, v_loan_44, v_desk, ((CURRENT_DATE - 2) + TIME '10:44') AT TIME ZONE 'Asia/Kolkata'),
        (v_book_9780143429012, 'ISSUE', -1, 2, 2, 2, 1, NULL, NULL, v_loan_47, v_desk, ((CURRENT_DATE - 1) + TIME '10:47') AT TIME ZONE 'Asia/Kolkata');
    INSERT INTO activity_log (actor_id, action, entity_type, entity_id, summary, metadata, created_at) VALUES
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9788173711466, 'Added "Wings of Fire" with 5 copies', '{}'::jsonb, ((CURRENT_DATE - 92) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780143029823, 'Added "Ignited Minds" with 3 copies', '{}'::jsonb, ((CURRENT_DATE - 92) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780143039655, 'Added "Malgudi Days" with 4 copies', '{}'::jsonb, ((CURRENT_DATE - 90) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780143039648, 'Added "The Guide" with 3 copies', '{}'::jsonb, ((CURRENT_DATE - 90) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780226568317, 'Added "Swami and Friends" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 90) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780812979657, 'Added "The God of Small Things" with 3 copies', '{}'::jsonb, ((CURRENT_DATE - 88) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9781524733155, 'Added "The Ministry of Utmost Happiness" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 88) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780618711666, 'Added "The Hungry Tide" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 86) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780312428594, 'Added "Sea of Poppies" with 3 copies', '{}'::jsonb, ((CURRENT_DATE - 86) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780618329960, 'Added "The Shadow Lines" with 1 copy', '{}'::jsonb, ((CURRENT_DATE - 86) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780141439518, 'Added "Pride and Prejudice" with 3 copies', '{}'::jsonb, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780451524935, 'Added "1984" with 4 copies', '{}'::jsonb, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780061120084, 'Added "To Kill a Mockingbird" with 3 copies', '{}'::jsonb, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780743273565, 'Added "The Great Gatsby" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780143058144, 'Added "Crime and Punishment" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780141441146, 'Added "Jane Eyre" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780142437247, 'Added "Moby-Dick" with 1 copy', '{}'::jsonb, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780060883287, 'Added "One Hundred Years of Solitude" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780156012195, 'Added "The Little Prince" with 3 copies', '{}'::jsonb, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780451526342, 'Added "Animal Farm" with 3 copies', '{}'::jsonb, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780385474542, 'Added "Things Fall Apart" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 85) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9789381020258, 'Added "Ponniyin Selvan" with 3 copies', '{}'::jsonb, ((CURRENT_DATE - 84) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780802128805, 'Added "One Part Woman" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 84) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780143429012, 'Added "Pyre" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 84) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780060786526, 'Added "A Suitable Boy" with 1 copy', '{}'::jsonb, ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780812976533, 'Added "Midnight''s Children" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780618485222, 'Added "The Namesake" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780395927205, 'Added "Interpreter of Maladies" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 80) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9781400030651, 'Added "A Fine Balance" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 78) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9781416562603, 'Added "The White Tiger" with 3 copies', '{}'::jsonb, ((CURRENT_DATE - 78) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780143333388, 'Added "The Room on the Roof" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 78) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780143031031, 'Added "The Discovery of India" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 76) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780143065883, 'Added "Train to Pakistan" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 76) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780132350884, 'Added "Clean Code" with 3 copies', '{}'::jsonb, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9781449373320, 'Added "Designing Data-Intensive Applications" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780134685991, 'Added "Effective Java" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780201616224, 'Added "The Pragmatic Programmer" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780134757599, 'Added "Refactoring" with 1 copy', '{}'::jsonb, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780201633610, 'Added "Design Patterns" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780262033848, 'Added "Introduction to Algorithms" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 70) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780062316097, 'Added "Sapiens" with 3 copies', '{}'::jsonb, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780593189641, 'Added "Atomic Habits" with 4 copies', '{}'::jsonb, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780374533557, 'Added "Thinking, Fast and Slow" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780857197689, 'Added "The Psychology of Money" with 3 copies', '{}'::jsonb, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780525510673, 'Added "Educated" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780553380163, 'Added "A Brief History of Time" with 2 copies', '{}'::jsonb, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'BOOK_CREATED', 'BOOK', v_book_9780060731335, 'Added "Freakonomics" with 1 copy', '{}'::jsonb, ((CURRENT_DATE - 65) + TIME '09:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'STOCK_ADDED', 'BOOK', v_book_9788173711466, 'Added 2 copies of "Wings of Fire"', '{}'::jsonb, ((CURRENT_DATE - 60) + TIME '11:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_0, 'Issued "Malgudi Days" to Priya Raman', '{}'::jsonb, ((CURRENT_DATE - 58) + TIME '10:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_1, 'Issued "Wings of Fire" to Arun Kumar Velu', '{}'::jsonb, ((CURRENT_DATE - 57) + TIME '10:01') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_ISSUED', 'LOAN', v_loan_2, 'Issued "1984" to Meena Sundaresan', '{}'::jsonb, ((CURRENT_DATE - 56) + TIME '10:02') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_3, 'Issued "Clean Code" to Vignesh Subramanian', '{}'::jsonb, ((CURRENT_DATE - 55) + TIME '10:03') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_4, 'Issued "Sapiens" to Anitha Krishnan', '{}'::jsonb, ((CURRENT_DATE - 54) + TIME '10:04') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_5, 'Issued "Atomic Habits" to Ganesh Ramasamy', '{}'::jsonb, ((CURRENT_DATE - 53) + TIME '10:05') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_ISSUED', 'LOAN', v_loan_6, 'Issued "The God of Small Things" to Divya Balasubramanian', '{}'::jsonb, ((CURRENT_DATE - 52) + TIME '10:06') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_7, 'Issued "Pride and Prejudice" to Sowmya Venkatesh', '{}'::jsonb, ((CURRENT_DATE - 51) + TIME '10:07') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_8, 'Issued "Designing Data-Intensive Applications" to Harish Gopalakrishnan', '{}'::jsonb, ((CURRENT_DATE - 50) + TIME '10:08') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_20, 'Issued "A Suitable Boy" to Karthikeyan Murugesan', '{}'::jsonb, ((CURRENT_DATE - 50) + TIME '10:20') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'STOCK_ADDED', 'BOOK', v_book_9780593189641, 'Added 1 copy of "Atomic Habits"', '{}'::jsonb, ((CURRENT_DATE - 50) + TIME '11:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_9, 'Issued "To Kill a Mockingbird" to Senthil Nathan', '{}'::jsonb, ((CURRENT_DATE - 49) + TIME '10:09') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_ISSUED', 'LOAN', v_loan_10, 'Issued "The Little Prince" to Nandhini Sekar', '{}'::jsonb, ((CURRENT_DATE - 48) + TIME '10:10') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_ISSUED', 'LOAN', v_loan_26, 'Issued "A Fine Balance" to Rajesh Kannan', '{}'::jsonb, ((CURRENT_DATE - 48) + TIME '10:26') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_11, 'Issued "The Psychology of Money" to Prakash Annamalai', '{}'::jsonb, ((CURRENT_DATE - 47) + TIME '10:11') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_0, 'Returned "Malgudi Days" from Priya Raman', '{}'::jsonb, ((CURRENT_DATE - 47) + TIME '16:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_12, 'Issued "Sea of Poppies" to Kavitha Rajendran', '{}'::jsonb, ((CURRENT_DATE - 46) + TIME '10:12') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_13, 'Issued "Ponniyin Selvan" to Muthukumar Pandian', '{}'::jsonb, ((CURRENT_DATE - 45) + TIME '10:13') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_21, 'Issued "Sapiens" to Lakshmi Narayanan', '{}'::jsonb, ((CURRENT_DATE - 45) + TIME '10:21') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_1, 'Returned "Wings of Fire" from Arun Kumar Velu', '{}'::jsonb, ((CURRENT_DATE - 45) + TIME '16:01') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_ISSUED', 'LOAN', v_loan_14, 'Issued "The Guide" to Revathi Chandrasekar', '{}'::jsonb, ((CURRENT_DATE - 44) + TIME '10:14') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_RETURNED', 'LOAN', v_loan_2, 'Returned "1984" from Meena Sundaresan', '{}'::jsonb, ((CURRENT_DATE - 44) + TIME '16:02') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_15, 'Issued "Effective Java" to Janani Mohan', '{}'::jsonb, ((CURRENT_DATE - 43) + TIME '10:15') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_3, 'Returned "Clean Code" from Vignesh Subramanian', '{}'::jsonb, ((CURRENT_DATE - 43) + TIME '16:03') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_ISSUED', 'LOAN', v_loan_22, 'Issued "Thinking, Fast and Slow" to Senthil Nathan', '{}'::jsonb, ((CURRENT_DATE - 42) + TIME '10:22') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_4, 'Returned "Sapiens" from Anitha Krishnan', '{}'::jsonb, ((CURRENT_DATE - 42) + TIME '16:04') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_5, 'Returned "Atomic Habits" from Ganesh Ramasamy', '{}'::jsonb, ((CURRENT_DATE - 41) + TIME '16:05') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_16, 'Issued "Atomic Habits" to Balaji Ravichandran', '{}'::jsonb, ((CURRENT_DATE - 40) + TIME '10:16') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_23, 'Issued "The Namesake" to Kavitha Rajendran', '{}'::jsonb, ((CURRENT_DATE - 40) + TIME '10:23') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_RETURNED', 'LOAN', v_loan_6, 'Returned "The God of Small Things" from Divya Balasubramanian', '{}'::jsonb, ((CURRENT_DATE - 40) + TIME '16:06') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_7, 'Returned "Pride and Prejudice" from Sowmya Venkatesh', '{}'::jsonb, ((CURRENT_DATE - 39) + TIME '16:07') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_17, 'Issued "Animal Farm" to Deepa Thirumalai', '{}'::jsonb, ((CURRENT_DATE - 38) + TIME '10:17') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_8, 'Returned "Designing Data-Intensive Applications" from Harish Gopalakrishnan', '{}'::jsonb, ((CURRENT_DATE - 38) + TIME '16:08') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_9, 'Returned "To Kill a Mockingbird" from Senthil Nathan', '{}'::jsonb, ((CURRENT_DATE - 37) + TIME '16:09') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_ISSUED', 'LOAN', v_loan_18, 'Issued "The White Tiger" to Saravanan Ilango', '{}'::jsonb, ((CURRENT_DATE - 36) + TIME '10:18') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_RETURNED', 'LOAN', v_loan_10, 'Returned "The Little Prince" from Nandhini Sekar', '{}'::jsonb, ((CURRENT_DATE - 36) + TIME '16:10') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_24, 'Issued "Midnight''s Children" to Revathi Chandrasekar', '{}'::jsonb, ((CURRENT_DATE - 35) + TIME '10:24') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_11, 'Returned "The Psychology of Money" from Prakash Annamalai', '{}'::jsonb, ((CURRENT_DATE - 35) + TIME '16:11') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_19, 'Issued "Wings of Fire" to Keerthana Pillai', '{}'::jsonb, ((CURRENT_DATE - 34) + TIME '10:19') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_12, 'Returned "Sea of Poppies" from Kavitha Rajendran', '{}'::jsonb, ((CURRENT_DATE - 34) + TIME '16:12') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_25, 'Issued "The Great Gatsby" to Ashwin Selvaraj', '{}'::jsonb, ((CURRENT_DATE - 33) + TIME '10:25') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_13, 'Returned "Ponniyin Selvan" from Muthukumar Pandian', '{}'::jsonb, ((CURRENT_DATE - 33) + TIME '16:13') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_RETURNED', 'LOAN', v_loan_14, 'Returned "The Guide" from Revathi Chandrasekar', '{}'::jsonb, ((CURRENT_DATE - 32) + TIME '16:14') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_15, 'Returned "Effective Java" from Janani Mohan', '{}'::jsonb, ((CURRENT_DATE - 31) + TIME '16:15') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_27, 'Issued "Jane Eyre" to Sowmya Venkatesh', '{}'::jsonb, ((CURRENT_DATE - 30) + TIME '10:27') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'STOCK_REMOVED', 'BOOK', v_book_9780451524935, 'Removed 1 copy of "1984" (damaged)', '{}'::jsonb, ((CURRENT_DATE - 30) + TIME '11:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_28, 'Issued "Educated" to Harish Gopalakrishnan', '{}'::jsonb, ((CURRENT_DATE - 29) + TIME '10:28') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_29, 'Issued "The Discovery of India" to Prakash Annamalai', '{}'::jsonb, ((CURRENT_DATE - 28) + TIME '10:29') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_16, 'Returned "Atomic Habits" from Balaji Ravichandran', '{}'::jsonb, ((CURRENT_DATE - 28) + TIME '16:16') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_17, 'Returned "Animal Farm" from Deepa Thirumalai', '{}'::jsonb, ((CURRENT_DATE - 26) + TIME '16:17') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_21, 'Returned "Sapiens" from Lakshmi Narayanan, 5 days late', '{}'::jsonb, ((CURRENT_DATE - 26) + TIME '16:21') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_38, 'Issued "Design Patterns" to Senthil Nathan', '{}'::jsonb, ((CURRENT_DATE - 25) + TIME '10:38') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'STOCK_ADDED', 'BOOK', v_book_9780132350884, 'Added 1 copy of "Clean Code"', '{}'::jsonb, ((CURRENT_DATE - 25) + TIME '11:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_20, 'Returned "A Suitable Boy" from Karthikeyan Murugesan', '{}'::jsonb, ((CURRENT_DATE - 25) + TIME '16:20') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_RETURNED', 'LOAN', v_loan_18, 'Returned "The White Tiger" from Saravanan Ilango', '{}'::jsonb, ((CURRENT_DATE - 24) + TIME '16:18') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_23, 'Returned "The Namesake" from Kavitha Rajendran, 3 days late', '{}'::jsonb, ((CURRENT_DATE - 23) + TIME '16:23') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_35, 'Issued "Moby-Dick" to Karthikeyan Murugesan', '{}'::jsonb, ((CURRENT_DATE - 22) + TIME '10:35') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_19, 'Returned "Wings of Fire" from Keerthana Pillai', '{}'::jsonb, ((CURRENT_DATE - 22) + TIME '16:19') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_39, 'Issued "Designing Data-Intensive Applications" to Vignesh Subramanian', '{}'::jsonb, ((CURRENT_DATE - 20) + TIME '10:39') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_RETURNED', 'LOAN', v_loan_22, 'Returned "Thinking, Fast and Slow" from Senthil Nathan, 8 days late', '{}'::jsonb, ((CURRENT_DATE - 20) + TIME '16:22') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_36, 'Issued "Refactoring" to Lakshmi Narayanan', '{}'::jsonb, ((CURRENT_DATE - 19) + TIME '10:36') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'STOCK_REMOVED', 'BOOK', v_book_9780143065883, 'Removed 1 copy of "Train to Pakistan" (lost)', '{}'::jsonb, ((CURRENT_DATE - 18) + TIME '11:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_ISSUED', 'LOAN', v_loan_37, 'Issued "Freakonomics" to Divya Balasubramanian', '{}'::jsonb, ((CURRENT_DATE - 17) + TIME '10:37') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_24, 'Returned "Midnight''s Children" from Revathi Chandrasekar, 6 days late', '{}'::jsonb, ((CURRENT_DATE - 15) + TIME '16:24') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_33, 'Issued "The Shadow Lines" to Arun Kumar Velu', '{}'::jsonb, ((CURRENT_DATE - 14) + TIME '10:33') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_ISSUED', 'LOAN', v_loan_34, 'Issued "1984" to Meena Sundaresan', '{}'::jsonb, ((CURRENT_DATE - 14) + TIME '10:34') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_25, 'Returned "The Great Gatsby" from Ashwin Selvaraj, 7 days late', '{}'::jsonb, ((CURRENT_DATE - 12) + TIME '16:25') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_29, 'Returned "The Discovery of India" from Prakash Annamalai, 2 days late', '{}'::jsonb, ((CURRENT_DATE - 12) + TIME '16:29') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_46, 'Issued "Swami and Friends" to Keerthana Pillai', '{}'::jsonb, ((CURRENT_DATE - 11) + TIME '10:46') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_28, 'Returned "Educated" from Harish Gopalakrishnan, 4 days late', '{}'::jsonb, ((CURRENT_DATE - 11) + TIME '16:28') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_30, 'Issued "Atomic Habits" to Priya Raman', '{}'::jsonb, ((CURRENT_DATE - 10) + TIME '10:30') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RETURNED', 'LOAN', v_loan_27, 'Returned "Jane Eyre" from Sowmya Venkatesh, 6 days late', '{}'::jsonb, ((CURRENT_DATE - 10) + TIME '16:27') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_42, 'Issued "Introduction to Algorithms" to Nandhini Sekar', '{}'::jsonb, ((CURRENT_DATE - 9) + TIME '10:42') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_RETURNED', 'LOAN', v_loan_26, 'Returned "A Fine Balance" from Rajesh Kannan, 25 days late', '{}'::jsonb, ((CURRENT_DATE - 9) + TIME '16:26') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_31, 'Issued "Clean Code" to Priya Raman', '{}'::jsonb, ((CURRENT_DATE - 8) + TIME '10:31') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_45, 'Issued "Swami and Friends" to Balaji Ravichandran', '{}'::jsonb, ((CURRENT_DATE - 7) + TIME '10:45') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_40, 'Issued "The Psychology of Money" to Anitha Krishnan', '{}'::jsonb, ((CURRENT_DATE - 6) + TIME '10:40') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_32, 'Issued "Ponniyin Selvan" to Priya Raman', '{}'::jsonb, ((CURRENT_DATE - 5) + TIME '10:32') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'LOAN_ISSUED', 'LOAN', v_loan_41, 'Issued "To Kill a Mockingbird" to Ganesh Ramasamy', '{}'::jsonb, ((CURRENT_DATE - 4) + TIME '10:41') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_43, 'Issued "The God of Small Things" to Deepa Thirumalai', '{}'::jsonb, ((CURRENT_DATE - 3) + TIME '10:43') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_44, 'Issued "Wings of Fire" to Janani Mohan', '{}'::jsonb, ((CURRENT_DATE - 2) + TIME '10:44') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_ISSUED', 'LOAN', v_loan_47, 'Issued "Pyre" to Ashwin Selvaraj', '{}'::jsonb, ((CURRENT_DATE - 1) + TIME '10:47') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_23, 'Registered Malathi Duraisamy', '{}'::jsonb, ((CURRENT_DATE - 500) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_0, 'Registered Priya Raman', '{}'::jsonb, ((CURRENT_DATE - 420) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_1, 'Registered Arun Kumar Velu', '{}'::jsonb, ((CURRENT_DATE - 400) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_2, 'Registered Meena Sundaresan', '{}'::jsonb, ((CURRENT_DATE - 380) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_3, 'Registered Karthikeyan Murugesan', '{}'::jsonb, ((CURRENT_DATE - 360) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_4, 'Registered Lakshmi Narayanan', '{}'::jsonb, ((CURRENT_DATE - 350) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_5, 'Registered Divya Balasubramanian', '{}'::jsonb, ((CURRENT_DATE - 340) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_6, 'Registered Senthil Nathan', '{}'::jsonb, ((CURRENT_DATE - 330) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_7, 'Registered Kavitha Rajendran', '{}'::jsonb, ((CURRENT_DATE - 320) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_24, 'Registered Rajesh Kannan', '{}'::jsonb, ((CURRENT_DATE - 310) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_8, 'Registered Muthukumar Pandian', '{}'::jsonb, ((CURRENT_DATE - 300) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_9, 'Registered Revathi Chandrasekar', '{}'::jsonb, ((CURRENT_DATE - 290) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_10, 'Registered Vignesh Subramanian', '{}'::jsonb, ((CURRENT_DATE - 270) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_11, 'Registered Anitha Krishnan', '{}'::jsonb, ((CURRENT_DATE - 260) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_12, 'Registered Ganesh Ramasamy', '{}'::jsonb, ((CURRENT_DATE - 250) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_13, 'Registered Sowmya Venkatesh', '{}'::jsonb, ((CURRENT_DATE - 240) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_14, 'Registered Harish Gopalakrishnan', '{}'::jsonb, ((CURRENT_DATE - 220) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_15, 'Registered Nandhini Sekar', '{}'::jsonb, ((CURRENT_DATE - 200) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_16, 'Registered Prakash Annamalai', '{}'::jsonb, ((CURRENT_DATE - 180) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_17, 'Registered Deepa Thirumalai', '{}'::jsonb, ((CURRENT_DATE - 160) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_18, 'Registered Saravanan Ilango', '{}'::jsonb, ((CURRENT_DATE - 140) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_19, 'Registered Janani Mohan', '{}'::jsonb, ((CURRENT_DATE - 120) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_20, 'Registered Balaji Ravichandran', '{}'::jsonb, ((CURRENT_DATE - 100) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_21, 'Registered Keerthana Pillai', '{}'::jsonb, ((CURRENT_DATE - 80) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'MEMBER_CREATED', 'MEMBER', v_member_22, 'Registered Ashwin Selvaraj', '{}'::jsonb, ((CURRENT_DATE - 60) + TIME '12:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RENEWED', 'LOAN', v_loan_20, 'Renewed "A Suitable Boy" for Karthikeyan Murugesan', '{}'::jsonb, ((CURRENT_DATE - 38) + TIME '11:30') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'FINE_CREATED', 'FINE', NULL, 'Fined Lakshmi Narayanan ₹25 for returning "Sapiens" 5 days late', '{}'::jsonb, ((CURRENT_DATE - 26) + TIME '16:21') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'FINE_PAID', 'FINE', NULL, 'Collected ₹25 from Lakshmi Narayanan', '{}'::jsonb, ((CURRENT_DATE - 24) + TIME '15:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'FINE_CREATED', 'FINE', NULL, 'Fined Senthil Nathan ₹40 for returning "Thinking, Fast and Slow" 8 days late', '{}'::jsonb, ((CURRENT_DATE - 20) + TIME '16:22') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'FINE_PAID', 'FINE', NULL, 'Collected ₹40 from Senthil Nathan', '{}'::jsonb, ((CURRENT_DATE - 18) + TIME '15:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'FINE_CREATED', 'FINE', NULL, 'Fined Kavitha Rajendran ₹15 for returning "The Namesake" 3 days late', '{}'::jsonb, ((CURRENT_DATE - 23) + TIME '16:23') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'FINE_WAIVED', 'FINE', NULL, 'Waived ₹15 for Kavitha Rajendran: medical reasons', '{}'::jsonb, ((CURRENT_DATE - 22) + TIME '15:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'FINE_CREATED', 'FINE', NULL, 'Fined Revathi Chandrasekar ₹30 for returning "Midnight''s Children" 6 days late', '{}'::jsonb, ((CURRENT_DATE - 15) + TIME '16:24') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'FINE_CREATED', 'FINE', NULL, 'Fined Ashwin Selvaraj ₹35 for returning "The Great Gatsby" 7 days late', '{}'::jsonb, ((CURRENT_DATE - 12) + TIME '16:25') AT TIME ZONE 'Asia/Kolkata'),
        (v_admin, 'FINE_CREATED', 'FINE', NULL, 'Fined Rajesh Kannan ₹125 for returning "A Fine Balance" 25 days late', '{}'::jsonb, ((CURRENT_DATE - 9) + TIME '16:26') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'FINE_CREATED', 'FINE', NULL, 'Fined Sowmya Venkatesh ₹30 for returning "Jane Eyre" 6 days late', '{}'::jsonb, ((CURRENT_DATE - 10) + TIME '16:27') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'FINE_PAID', 'FINE', NULL, 'Collected ₹30 from Sowmya Venkatesh', '{}'::jsonb, ((CURRENT_DATE - 8) + TIME '15:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'FINE_CREATED', 'FINE', NULL, 'Fined Harish Gopalakrishnan ₹20 for returning "Educated" 4 days late', '{}'::jsonb, ((CURRENT_DATE - 11) + TIME '16:28') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'FINE_CREATED', 'FINE', NULL, 'Fined Prakash Annamalai ₹10 for returning "The Discovery of India" 2 days late', '{}'::jsonb, ((CURRENT_DATE - 12) + TIME '16:29') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'FINE_PAID', 'FINE', NULL, 'Collected ₹10 from Prakash Annamalai', '{}'::jsonb, ((CURRENT_DATE - 10) + TIME '15:00') AT TIME ZONE 'Asia/Kolkata'),
        (v_desk, 'LOAN_RENEWED', 'LOAN', v_loan_39, 'Renewed "Designing Data-Intensive Applications" for Vignesh Subramanian', '{}'::jsonb, ((CURRENT_DATE - 8) + TIME '11:30') AT TIME ZONE 'Asia/Kolkata');
END
$$;
