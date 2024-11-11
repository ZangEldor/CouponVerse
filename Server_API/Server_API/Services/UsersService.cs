using Server_API.Models;
using Microsoft.Extensions.Options;
using MongoDB.Driver;

namespace Server_API.Services;

public class UsersService
{
    private readonly IMongoCollection<User> _usersCollection;

    public UsersService(
        IOptions<CouponVerseDatabaseSettings> couponVerseDatabaseSettings)
    {
        var mongoClient = new MongoClient(
            couponVerseDatabaseSettings.Value.ConnectionString);

        var mongoDatabase = mongoClient.GetDatabase(
            couponVerseDatabaseSettings.Value.DatabaseName);

        _usersCollection = mongoDatabase.GetCollection<User>(
            couponVerseDatabaseSettings.Value.UsersCollectionName);
    }

    public async Task<List<User>> GetAsyncAll() =>
        await _usersCollection.Find(_ => true).ToListAsync();

    public async Task<User?> GetAsyncByUserName(string userName) =>
        await _usersCollection.Find(x => x.UserName == userName).FirstOrDefaultAsync();

    public async Task CreateAsync(User newUser) =>
        await _usersCollection.InsertOneAsync(newUser);

    public async Task UpdateAsync(string userName, User updatedUser) =>
        await _usersCollection.ReplaceOneAsync(x => x.UserName == userName, updatedUser);

    public async Task RemoveAsync(string userName) =>
        await _usersCollection.DeleteOneAsync(x => x.UserName == userName);
}