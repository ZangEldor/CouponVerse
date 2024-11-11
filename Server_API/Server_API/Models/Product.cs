using MongoDB.Bson.Serialization.Attributes;

namespace Server_API.Models
{
    public class Product
    {
        public string? title { get; set; }

        public string? imgUrl { get; set; }

        public string? productURL { get; set; }

        public string? stars { get; set; }
        
        public string? category_name { get; set; }
        public string? price { get; set; }

        public Boolean? isBestSeller { get; set; }
    }
}
