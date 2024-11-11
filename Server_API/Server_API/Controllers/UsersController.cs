using Microsoft.AspNetCore.Mvc;
using Server_API.Helpers;
using Server_API.Models;
using Server_API.Services;
using YourNamespace.Models;

namespace Server_API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class UsersController : ControllerBase
    {
        private readonly UsersService _usersService;
        private readonly CouponsService _couponsService;
        private readonly GroupService _groupService;
        private readonly MLModelsService _mlModelsService;

        public UsersController(UsersService usersService, CouponsService couponsService, GroupService groupService, MLModelsService mlModelsService)
        {
            _usersService = usersService;
            _couponsService = couponsService;
            _groupService = groupService;
            _mlModelsService = mlModelsService;
        }
        /*
        [HttpGet]
        public async Task<List<User>> GetAllUsers() =>
            await _usersService.GetAsync();

        [HttpGet("{username}")]
        public async Task<ActionResult<User>> GetUser(string username)
        {
            var user = await _usersService.GetAsync(username);

            if (user is null)
            {
                return NotFound();
            }

            return user;
        }     
        */

        [HttpGet("checkUserExists")]
        public async Task<ActionResult> checkUserExists(string username)
        {
            var userByName = await _usersService.GetAsyncByUserName(username);

            if (userByName is not null)
            {
                return Ok();
            }
 
            return BadRequest();

        }

        [HttpPost("UsersFromList")]
        public async Task<ActionResult<List<User>>> getUsersFromList(List<String> users_list)
        {
            var output = new List<User>();

            // Loop through the input list
            foreach (string username in users_list)
            {
                var userByName = await _usersService.GetAsyncByUserName(username);

                if (userByName is null)
                {
                    return BadRequest();
                }
                output.Add(userByName);
            }
             return Ok(output);

        }
        [HttpGet("login")]
        public async Task<ActionResult<User>> Login(string username, string password)
        {
            var userByName = await _usersService.GetAsyncByUserName(username);

            if ((userByName is not null) && (PasswordHelper.HashPassword(password, Convert.FromBase64String(userByName.salt)) == userByName.Password))
            {
                return Ok(userByName);
            }
            return BadRequest();

        }
        [HttpPost("register")]
        public async Task<IActionResult> Register(UserInput newUser)
        {
            var salt = PasswordHelper.GenerateSalt(16);
            var hashedPassword = PasswordHelper.HashPassword(newUser.password, salt);
            List<double> averageEmbeddings = new List<double>(new double[384]);
            User toInsertNewUser = new User(newUser.userName, hashedPassword, newUser.picture, Convert.ToBase64String(salt), averageEmbeddings);
            UserCoupons toInsertNewUserCoupons = new UserCoupons(newUser.userName);
            var user = await _usersService.GetAsyncByUserName(newUser.userName);
            if (user != null)
            {
                return BadRequest();
            }
            await _usersService.CreateAsync(toInsertNewUser);
            await _couponsService.CreateAsync(toInsertNewUserCoupons);
            return Ok();
        }

        [HttpPut("{userName}")]
        public async Task<IActionResult> UpdateUser(string userName, UserInput updatedUser)
        {
            var user = await _usersService.GetAsyncByUserName(userName);

            if (user is null)
            {
                return NotFound();
            }
            if (updatedUser.userName is not null)
            {
                user.UserName = updatedUser.userName;
                var userCoupons = await _couponsService.GetAsyncByUserName(userName);
                if (userCoupons != null)
                {
                    userCoupons.UserName = updatedUser.userName;
                    await _couponsService.UpdateAsync(userName, userCoupons);
                }
            }
            if (updatedUser.password is not null)
            {
                user.Password = updatedUser.password;
            }
            if (updatedUser.picture is not null)
            {
                user.Picture = updatedUser.picture;
            }
            await _usersService.UpdateAsync(userName, user);
            await _groupService.UpdateUsernameAsync(userName, updatedUser.userName);
            return NoContent();
        }
        [HttpPut("password")]
        public async Task<IActionResult> UpdatePassword(string userName, string password)
        {
            var user = await _usersService.GetAsyncByUserName(userName);

            if (user is null)
            {
                return NotFound();
            }
            var salt = PasswordHelper.GenerateSalt(16);
            var hashedPassword = PasswordHelper.HashPassword(password, salt);

            user.Password = hashedPassword;
            user.salt = Convert.ToBase64String(salt);

            await _usersService.UpdateAsync(userName, user);

            return NoContent();
        }
        [HttpDelete("{userName}")]
        public async Task<IActionResult> DeleteUser(string userName)
        {
            var user = await _usersService.GetAsyncByUserName(userName);

            if (user is null)
            {
                return NotFound();
            }

            await _usersService.RemoveAsync(userName);
            await _couponsService.RemoveAsync(userName);

            return NoContent();
        }

        [HttpPost("updateAverageEmbedding")]
        public async Task<IActionResult> UpdateAverageEmbedding(string userName, string coupon)
        {
            var user = await _usersService.GetAsyncByUserName(userName);

            if (user is null)
            {
                return NotFound();
            }
            try
            {
                var new_embedding = await _mlModelsService.GetEmbedding(coupon);
                var userCoupons = await _couponsService.GetAsyncByUserName(userName);
                var couponsLength = userCoupons.Coupons.Count;
                // new_avg = ((old_avg*old_count)+new_embedding) / (old_count + 1)
                var old_avg_summed =  user.AverageEmbedding.Select(n => n * (couponsLength-1)).ToList();
                var new_avg_summed =     old_avg_summed.Zip(new_embedding, (a, b) => a + b).ToList();
                var new_avg = new_avg_summed.Select(n => n / couponsLength).ToList();
                user.AverageEmbedding = new_avg;
                await _usersService.UpdateAsync(userName,user);
                return Ok();

            }
            catch (HttpRequestException ex)
            {
                return StatusCode(500, ex.Message);
            }
        }

        [HttpPost("updateAverageEmbeddingOnEdit")]
        public async Task<IActionResult> UpdateAverageEmbeddingEdited(string userName, string coupon, string oldCoupon)
        {
            var user = await _usersService.GetAsyncByUserName(userName);

            if (user is null)
            {
                return NotFound();
            }
            try
            {
                var old_embedding = await _mlModelsService.GetEmbedding(oldCoupon);
                var new_embedding = await _mlModelsService.GetEmbedding(coupon);
                var userCoupons = await _couponsService.GetAsyncByUserName(userName);
                var couponsLength = userCoupons.Coupons.Count;
                // new_avg = ((old_avg * count) + new_embedding - old_embedding) / count
                var old_avg_summed = user.AverageEmbedding.Select(n => n * couponsLength).ToList();
                var temp_avg_summed = old_avg_summed.Zip(new_embedding, (a, b) => a + b).ToList();
                var new_avg_summed = temp_avg_summed.Zip(old_embedding, (a, b) => a - b).ToList();
                var new_avg = new_avg_summed.Select(n => n / couponsLength).ToList();
                user.AverageEmbedding = new_avg;
                await _usersService.UpdateAsync(userName, user);
                return Ok();

            }
            catch (HttpRequestException ex)
            {
                return StatusCode(500, ex.Message);
            }
        }

    }
}