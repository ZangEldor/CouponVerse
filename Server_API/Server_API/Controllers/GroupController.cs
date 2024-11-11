using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using Server_API.Hubs;
using Server_API.Models;
using Server_API.Services;
using System.Collections.Generic;
using System.Threading.Tasks;


namespace Server_API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class GroupsController : ControllerBase
    {
        private readonly GroupService _groupService;
        private readonly IHubContext<GroupsHub> _hubContext;
        public GroupsController(GroupService groupService,IHubContext<GroupsHub> hubContext)
        {
            _groupService = groupService;
            _hubContext = hubContext;
        }

        [HttpGet]
        public async Task<ActionResult<List<Group>>> Get() =>
            await _groupService.GetAllAsync();

        [HttpGet("id/{id}")]
        public async Task<ActionResult<Group>> Get(string id)
        {
            var group = await _groupService.GetByIdAsync(id);

            if (group == null)
            {
                return NotFound();
            }

            return group;
        }

        [HttpGet("{username}/getGroups")]
        public async Task<ActionResult<List<Group>>> GetGroups(string username)
        {
            var groupList = await _groupService.GetUserGroupsAsync(username);

            if (groupList == null)
            {
                return NotFound();
            }

            return groupList;
        }

        [HttpPost]
        public async Task<ActionResult<Group>> Create(Group group)
        {
            await _groupService.CreateAsync(group);

            return CreatedAtAction(nameof(Get), new { id = group.Id }, group);
        }

        [HttpPut("{id}")]
        public async Task<IActionResult> Update(string id, Group groupIn)
        {
            var group = await _groupService.GetByIdAsync(id);

            if (group == null)
            {
                return NotFound();
            }

            await _groupService.UpdateAsync(id, groupIn);

            return NoContent();
        }
        [HttpGet("name/{groupName}")]
        public async Task<ActionResult<List<Group>>> GetByName(string groupName)
        {
            var groupsList = await _groupService.GetByNameAsync(groupName);

            if (groupsList == null)
            {
                return NotFound();
            }

            return groupsList;
        }
        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(string id)
        {
            var group = await _groupService.GetByIdAsync(id);

            if (group == null)
            {
                return NotFound();
            }

            await _groupService.RemoveAsync(id);

            return NoContent();
        }

        [HttpPost("{id}/addAdmin")]
        public async Task<IActionResult> AddAdmin(string id, [FromBody] string admin)
        {
            await _groupService.AddAdminAsync(id, admin);
            return NoContent();
        }

        [HttpPost("{id}/removeAdmin")]
        public async Task<IActionResult> RemoveAdmin(string id, [FromBody] string admin)
        {
            await _groupService.RemoveAdminAsync(id, admin);
            return NoContent();
        }

        [HttpPost("{id}/addUser")]
        public async Task<IActionResult> AddUser(string id, [FromBody] string user)
        {
            await _groupService.AddUserAsync(id, user);
            return NoContent();
        }

        [HttpPost("{id}/removeUser")]
        public async Task<IActionResult> RemoveUser(string id, [FromBody] string user)
        {
            await _groupService.RemoveUserAsync(id, user);
            return NoContent();
        }
        [HttpPost("{id}/updatePicture")]
        public async Task<IActionResult> UpdateGroupPicture(string id, string picture)
        {
            var group = await _groupService.GetByIdAsync(id);

            if (group == null)
            {
                return NotFound();
            }
            group.picture = picture;
            await _groupService.UpdateAsync(id, group);
            return NoContent();
        }
        [HttpPost("{id}/addMessage")]
        public async Task<IActionResult> AddMessage(string id, [FromBody] Message message)
        {
            await _groupService.AddMessageAsync(id, message);
            await _hubContext.Clients.All.SendAsync("ReceiveGroupUpdate",message,id);
            return NoContent();
        }
        [HttpPost("updateUsername")]
        public async Task<IActionResult> UpdateUsername([FromBody] UpdateUsernameRequest request)
        {
            if (string.IsNullOrWhiteSpace(request.OldUsername) || string.IsNullOrWhiteSpace(request.NewUsername))
            {
                return BadRequest("Usernames cannot be empty.");
            }

            await _groupService.UpdateUsernameAsync(request.OldUsername, request.NewUsername);

            return NoContent();
        }
    }


}
    

