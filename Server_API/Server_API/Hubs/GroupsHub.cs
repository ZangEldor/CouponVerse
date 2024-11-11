using Microsoft.AspNetCore.SignalR;
namespace Server_API.Hubs
{
public class GroupsHub : Hub
{
    public async Task SendMessages(string message,string id)
    {
        await Clients.All.SendAsync("ReceiveGroupUpdate",message,id);
    }
}
}