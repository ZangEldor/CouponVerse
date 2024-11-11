using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using Server_API.Models;
using System.Collections.Generic;

namespace Server_API.Models
{
    public class Group
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; }

        [BsonElement("name")]
        public string Name { get; set; }

        [BsonElement("admins")]
        public List<string> Admins { get; set; } = new List<string>();

        [BsonElement("users")]
        public List<string> Users { get; set; } = new List<string>();

        [BsonElement("messages")]
        public List<Message> Messages { get; set; } = new List<Message>();
        public string? picture { get; set; }
    }
}
