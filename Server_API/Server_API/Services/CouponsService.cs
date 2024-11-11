using Server_API.Models;
using Microsoft.Extensions.Options;
using MongoDB.Driver;
using YourNamespace.Models;

namespace Server_API.Services;

public class CouponsService
{
    private readonly IMongoCollection<UserCoupons> _usersCouponsCollection;

    public CouponsService(
        IOptions<CouponVerseDatabaseSettings> couponVerseDatabaseSettings)
    {
        var mongoClient = new MongoClient(
            couponVerseDatabaseSettings.Value.ConnectionString);

        var mongoDatabase = mongoClient.GetDatabase(
            couponVerseDatabaseSettings.Value.DatabaseName);

        _usersCouponsCollection = mongoDatabase.GetCollection<UserCoupons>(
            couponVerseDatabaseSettings.Value.CouponsCollectionName);
    }

    public async Task<List<UserCoupons>> GetAsyncAll() =>
        await _usersCouponsCollection.Find(_ => true).ToListAsync();

    public async Task<UserCoupons?> GetAsyncByUserName(string userName) =>
        await _usersCouponsCollection.Find(x => x.UserName == userName).FirstOrDefaultAsync();
   
    public async Task CreateAsync(UserCoupons newUserCoupons) =>
        await _usersCouponsCollection.InsertOneAsync(newUserCoupons);

    public async Task UpdateAsync(string userName, UserCoupons updatedUserCoupons) =>
        await _usersCouponsCollection.ReplaceOneAsync(x => x.UserName == userName, updatedUserCoupons);

    public async Task RemoveAsync(string userName) =>
        await _usersCouponsCollection.DeleteOneAsync(x => x.UserName == userName);

    public async Task AddCouponAsync(string userName,Coupon newCoupon)
    {
        var update = Builders<UserCoupons>.Update.Push(u => u.Coupons, newCoupon);
        await _usersCouponsCollection.UpdateOneAsync(u => u.UserName == userName, update);
    }
}