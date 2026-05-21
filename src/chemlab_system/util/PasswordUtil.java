package chemlab_system.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing utility using BCrypt (via jbcrypt-0.4.jar).
 *
 * Provides backward-compatible verification that handles both:
 * - Passwords already hashed with BCrypt (start with $2a$, $2b$, $2y$)
 * - Legacy plaintext passwords (migrated from the original system)
 *
 * When a legacy plaintext password is detected and matches, the caller
 * should re-hash and update the database so the account is silently upgraded.
 */
public class PasswordUtil {

    /**
     * Hashes a plaintext password using BCrypt with cost factor 12.
     * 
     * @param plaintext The raw password entered by the user.
     * @return A BCrypt hash string safe for storage.
     */
    public static String hash(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(12));
    }

    /**
     * Verifies a plaintext password against a stored value (BCrypt or legacy
     * plaintext).
     *
     * @param plaintext The raw password the user entered.
     * @param stored    The value stored in the database (may be BCrypt hash or
     *                  plaintext).
     * @return A {@link VerifyResult} with match status and whether rehashing is
     *         needed.
     */
    public static VerifyResult verify(String plaintext, String stored) {
        if (stored == null || stored.isEmpty()) {
            return new VerifyResult(false, false);
        }

        // Detect BCrypt hash by prefix
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            boolean matches = BCrypt.checkpw(plaintext, stored);
            return new VerifyResult(matches, false); // already hashed — no rehash needed
        }

        // Legacy plaintext comparison
        boolean matches = plaintext.equals(stored);
        // If it matched a plaintext, flag for rehash so the account gets upgraded
        return new VerifyResult(matches, matches);
    }

    /**
     * Result of a password verification.
     */
    public static class VerifyResult {
        /** Whether the plaintext password matches the stored value. */
        public final boolean matches;
        /**
         * Whether the stored value was plaintext and should be replaced with a BCrypt
         * hash.
         */
        public final boolean needsRehash;

        public VerifyResult(boolean matches, boolean needsRehash) {
            this.matches = matches;
            this.needsRehash = needsRehash;
        }
    }
}
