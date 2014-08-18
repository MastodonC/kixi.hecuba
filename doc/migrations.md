### 2014-08-18 Password Reset

* Database migration:

      ALTER TABLE users ADD reset_timestamp timestamp;
      ALTER TABLE users ADD reset_uuid text;
      CREATE INDEX users_reset_idx ON users (reset_uuid);

* Update .hecuba.edn
        :e-mail {:api-url "<url to post e-mails>"
                 :key "<API key>"
                 :hostname "<http://somename.com>"}
