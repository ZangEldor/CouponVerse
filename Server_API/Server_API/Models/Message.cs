using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System;

namespace Server_API.Models
{
    public class Message
    {
        [BsonElement("sender")]
        public string Sender { get; set; }

        [BsonElement("data")]
        public string Data { get; set; }

        [BsonElement("timestamp_string")]
        public string timestamp_string { get; set; }
    }
}
