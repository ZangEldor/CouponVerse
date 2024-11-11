using System;
using System.Security.Cryptography;
using System.Text;

namespace Server_API.Helpers
{
    public class PasswordHelper
    {
        public static byte[] GenerateSalt(int size)
        {
            var salt = new byte[size];
            RandomNumberGenerator.Fill(salt);
            return salt;
        }

        public static string HashPassword(string password, byte[] salt)
        {
            using (var sha256 = SHA256.Create())
            {
                var saltedPassword = Encoding.UTF8.GetBytes(password);
                var saltedPasswordWithSalt = new byte[saltedPassword.Length + salt.Length];

                Buffer.BlockCopy(saltedPassword, 0, saltedPasswordWithSalt, 0, saltedPassword.Length);
                Buffer.BlockCopy(salt, 0, saltedPasswordWithSalt, saltedPassword.Length, salt.Length);

                var hashedPassword = sha256.ComputeHash(saltedPasswordWithSalt);
                return Convert.ToBase64String(hashedPassword);
            }
        }
    }
}

