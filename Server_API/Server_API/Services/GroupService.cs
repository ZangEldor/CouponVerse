using Microsoft.Extensions.Options;
using MongoDB.Bson;
using MongoDB.Driver;
using Server_API.Models;
using System.Collections.Generic;
using System.Threading.Tasks;


namespace Server_API.Services
{
    public class GroupService
    {
        private readonly IMongoCollection<Group> _groups;

        public GroupService(IOptions<CouponVerseDatabaseSettings> couponVerseDatabaseSettings)
        {
            var mongoClient = new MongoClient(couponVerseDatabaseSettings.Value.ConnectionString);
            var mongoDatabase = mongoClient.GetDatabase(couponVerseDatabaseSettings.Value.DatabaseName);
            _groups = mongoDatabase.GetCollection<Group>(couponVerseDatabaseSettings.Value.GroupsCollectionName);
        }
        public async Task<List<Group>> GetByNameAsync(string groupName)
        {
            var filter = Builders<Group>.Filter.Regex(g => g.Name, new BsonRegularExpression(groupName, "i"));
            return await _groups.Find(filter).ToListAsync();
        }
        public async Task<List<Group>> GetAllAsync() =>
            await _groups.Find(group => true).ToListAsync();

        public async Task<Group> GetByIdAsync(string id) =>
            await _groups.Find<Group>(group => group.Id == id).FirstOrDefaultAsync();

        public async Task<List<Group>> GetUserGroupsAsync(string username)
        {
            var filter = Builders<Group>.Filter.AnyEq(g => g.Users, username);
            return await _groups.Find(filter).ToListAsync();
        }

        public async Task<Group> CreateAsync(Group group)
        {
            await _groups.InsertOneAsync(group);
            return group;
        }

        public async Task UpdateAsync(string id, Group groupIn) =>
            await _groups.ReplaceOneAsync(group => group.Id == id, groupIn);

        public async Task RemoveAsync(string id) =>
            await _groups.DeleteOneAsync(group => group.Id == id);

        public async Task AddAdminAsync(string groupId, string admin)
        {
            var filter = Builders<Group>.Filter.Eq(g => g.Id, groupId);
            var update = Builders<Group>.Update.AddToSet(g => g.Admins, admin);
            await _groups.UpdateOneAsync(filter, update);
        }

        public async Task RemoveAdminAsync(string groupId, string admin)
        {
            var filter = Builders<Group>.Filter.Eq(g => g.Id, groupId);
            var update = Builders<Group>.Update.Pull(g => g.Admins, admin);
            await _groups.UpdateOneAsync(filter, update);
        }

        public async Task AddUserAsync(string groupId, string user)
        {
            var filter = Builders<Group>.Filter.Eq(g => g.Id, groupId);
            var update = Builders<Group>.Update.AddToSet(g => g.Users, user);
            await _groups.UpdateOneAsync(filter, update);
        }

        public async Task RemoveUserAsync(string groupId, string user)
        {
            var filter = Builders<Group>.Filter.Eq(g => g.Id, groupId);
            var update = Builders<Group>.Update.Pull(g => g.Users, user);
            await _groups.UpdateOneAsync(filter, update);
        }

        public async Task AddMessageAsync(string groupId, Message message)
        {
            var filter = Builders<Group>.Filter.Eq(g => g.Id, groupId);
            var update = Builders<Group>.Update.AddToSet(g => g.Messages, message);
            await _groups.UpdateOneAsync(filter, update);
        }
        public async Task UpdateUsernameAsync(string oldUsername, string newUsername)
        {
            var filterAdmins = Builders<Group>.Filter.AnyEq(g => g.Admins, oldUsername);
            var filterUsers = Builders<Group>.Filter.AnyEq(g => g.Users, oldUsername);
            var filterMessages = Builders<Group>.Filter.ElemMatch(g => g.Messages, m => m.Sender == oldUsername);

            var updateAdmins = Builders<Group>.Update.Set("Admins.$", newUsername);
            var updateUsers = Builders<Group>.Update.Set("Users.$", newUsername);
            var updateMessages = Builders<Group>.Update.Set("Messages.$[elem].Sender", newUsername);

            var arrayFilters = new List<ArrayFilterDefinition>
    {
        new BsonDocumentArrayFilterDefinition<BsonDocument>(new BsonDocument("elem.Sender", oldUsername))
    };

            var updateOptions = new UpdateOptions { ArrayFilters = arrayFilters };

            await _groups.UpdateManyAsync(filterAdmins, updateAdmins);
            await _groups.UpdateManyAsync(filterUsers, updateUsers);
            await _groups.UpdateManyAsync(filterMessages, updateMessages, updateOptions);
        }

    }
}
