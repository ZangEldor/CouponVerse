using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace Server_API.Models
{
    public class User
    {
        
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }
        
        public string? UserName { get; set; }
        public string? Password { get; set; }
        public string? salt { get; set; }
        public string? Picture { get; set; }
        public List<double> AverageEmbedding { get; set; }
        public User(string? userNameArg, string? passwordArg,string? picture,string? salt, List<double>? averageEmbedding)
        {
            this.Id = null;
            this.UserName = userNameArg;
            this.Password = passwordArg;
            this.Picture = picture;
            this.salt = salt;
            this.AverageEmbedding = averageEmbedding;
        }
    }
}
