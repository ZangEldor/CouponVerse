using MongoDB.Bson.Serialization.Attributes;
using MongoDB.Bson;

namespace Server_API.Models
{
    public class UserInput
    {
        public string? userName { get; set; }
        public string? password { get; set; }
        public string? picture { get; set; }
    }
    public class CouponInput
    {
        public string? Title { get; set; }
        public string? Company { get; set; }
        public string? Category { get; set; }
        public string? Expire_Date { get; set; }
        public string? Use_Date { get; set; }
        public bool? Is_Used { get; set; }
        public string? Description { get; set; }
        public string? Code { get; set; }
        public string? Original_Text { get; set; }
        public string? Bought_From { get; set; }
    }
    public class UpdateUsernameRequest
    {
        public string OldUsername { get; set; }
        public string NewUsername { get; set; }
    }
    public class InferenceMLRecommendationsModelRequest
    {
        public List<double> Embeddings { get; set; }
  
    }
}
