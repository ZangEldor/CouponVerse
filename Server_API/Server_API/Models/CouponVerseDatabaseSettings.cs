namespace Server_API.Models
{
    public class CouponVerseDatabaseSettings
    {
        public string ConnectionString { get; set; } = null!;

        public string DatabaseName { get; set; } = null!;

        public string UsersCollectionName { get; set; } = null!;
        public string CouponsCollectionName { get; set; } = null!;
        public string GroupsCollectionName { get; set; } = null!;

    }
}
