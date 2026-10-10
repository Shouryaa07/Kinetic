package com.kinetic.dao;

import com.kinetic.db.Database;
import com.kinetic.model.AuthProvider;
import com.kinetic.model.User;
import java.util.List;
import java.util.Optional;

/** Database access for {@link User}. */
public class UserDao extends JdbcDao implements Dao<User, Long> {
    private static final String COLUMNS =
            "id, email, display_name, password_hash, provider, google_sub, avatar_url, weight_kg, height_cm, created_at";

    private static final RowMapper<User> MAPPER = rs -> {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setDisplayName(rs.getString("display_name"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setProvider(AuthProvider.valueOf(rs.getString("provider")));
        u.setGoogleSub(rs.getString("google_sub"));
        u.setAvatarUrl(rs.getString("avatar_url"));
        u.setWeightKg(rs.getDouble("weight_kg"));
        u.setHeightCm(rs.getInt("height_cm"));
        u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return u;
    };

    public UserDao(Database db) {
        super(db);
    }

    @Override
    public Optional<User> findById(Long id) {
        return queryOne("SELECT " + COLUMNS + " FROM users WHERE id = ?", MAPPER, id);
    }

    public Optional<User> findByEmail(String email) {
        return queryOne("SELECT " + COLUMNS + " FROM users WHERE email = ?", MAPPER, email);
    }

    public Optional<User> findByGoogleSub(String sub) {
        return queryOne("SELECT " + COLUMNS + " FROM users WHERE google_sub = ?", MAPPER, sub);
    }

    @Override
    public List<User> findAll() {
        return query("SELECT " + COLUMNS + " FROM users ORDER BY id", MAPPER);
    }

    @Override
    public User save(User u) {
        if (u.getId() == null) {
            long id = insert("INSERT INTO users (email, display_name, password_hash, provider, google_sub, "
                            + "avatar_url, weight_kg, height_cm) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    u.getEmail(), u.getDisplayName(), u.getPasswordHash(), u.getProvider().name(),
                    u.getGoogleSub(), u.getAvatarUrl(), u.getWeightKg(), u.getHeightCm());
            return findById(id).orElseThrow();
        }
        update("UPDATE users SET email = ?, display_name = ?, password_hash = ?, provider = ?, google_sub = ?, "
                        + "avatar_url = ?, weight_kg = ?, height_cm = ? WHERE id = ?",
                u.getEmail(), u.getDisplayName(), u.getPasswordHash(), u.getProvider().name(),
                u.getGoogleSub(), u.getAvatarUrl(), u.getWeightKg(), u.getHeightCm(), u.getId());
        return u;
    }

    @Override
    public boolean deleteById(Long id) {
        return update("DELETE FROM users WHERE id = ?", id) > 0;
    }
}
