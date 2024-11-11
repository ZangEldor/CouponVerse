using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace Server_API.Models
{
    public class Coupon
    {
        public string? Title { get; set; }

        public string? Company { get; set; }

        public string? Category { get; set; }

        public string? Expire_Date { get; set; }


        public bool? Is_Used { get; set; }

        public string? Description { get; set; }

        public string? Code { get; set; }


        public string? Bought_From { get; set; }
        public Coupon(string? title, string? company, string? category, string? expireDate, bool? isUsed, string? description, string? code, string? boughtFrom)
        {
            Title = title;
            Company = company;
            Category = category;
            Expire_Date = expireDate;
            Is_Used = isUsed;
            Description = description;
            Code = code;
            Bought_From = boughtFrom;
        }
    }
}
