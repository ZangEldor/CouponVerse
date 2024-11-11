using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using Server_API.Models;
using System.Collections.Generic;

namespace YourNamespace.Models
{
    public class UserCoupons
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        public string? UserName { get; set; }

        public List<Coupon> Coupons { get; set; } = new List<Coupon>();
        public UserCoupons(string? username) {
            UserName= username;
            Coupons = new List<Coupon>();
        }
    }
}
