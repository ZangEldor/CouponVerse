using Microsoft.AspNetCore.Mvc;
using Server_API.Models;
using Server_API.Services;

namespace Server_API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class CouponController : ControllerBase
    {
        private readonly CouponsService _couponsService;

        public CouponController(CouponsService couponsService)
        {
            _couponsService = couponsService;
        }

        [HttpGet("{userName}")]
        public async Task<IActionResult> GetCouponByIndexAsync(string userName)
        {
            var user = await _couponsService.GetAsyncByUserName(userName);

            if (user == null)
            {
                return NotFound();
            }
            return Ok(user.Coupons);
        }
        [HttpGet("{userName}/{index}")]
        public async Task<IActionResult> GetCouponByIndexAsync(string userName, int index)
        {
            var user = await _couponsService.GetAsyncByUserName(userName);

            if (user == null || index < 0 || index >= user.Coupons.Count)
            {
                return NotFound();
            }

            return Ok(user.Coupons[index]);
        }
        [HttpPost("addNewCoupon")]
        public async Task<IActionResult> AppendCoupon(string username,CouponInput newCoupon)
        {
            Coupon toInsertNewCoupon = new Coupon(newCoupon.Title, newCoupon.Company, newCoupon.Category, newCoupon.Expire_Date,
                                                  newCoupon.Is_Used, newCoupon.Description, newCoupon.Code, newCoupon.Bought_From);
            var userCoupon = await _couponsService.GetAsyncByUserName(username);
            if (userCoupon == null)
            {
                return BadRequest();
            }
            await _couponsService.AddCouponAsync(username,toInsertNewCoupon);
            return Ok();
        }

        [HttpPut("{userName}/{index}")]
        public async Task<IActionResult> UpdateCouponByIndexAsync(string userName, int index, CouponInput updatedCoupon)
        {
            var user = await _couponsService.GetAsyncByUserName(userName);
            var newCoupon = new Coupon(
               title: updatedCoupon.Title,
               company: updatedCoupon.Company,
               category: updatedCoupon.Category,
               expireDate: updatedCoupon.Expire_Date,
               isUsed: updatedCoupon.Is_Used,
               description: updatedCoupon.Description,
               code: updatedCoupon.Code,
               boughtFrom: updatedCoupon.Bought_From
            );
            if (user == null || index < 0 || index >= user.Coupons.Count)
            {
                return NotFound();
            }
            user.Coupons[index] = newCoupon;
            await _couponsService.UpdateAsync(userName,user);
            return Ok();
        }

        [HttpDelete("{userName}/{index}")]
        public async Task<IActionResult> DeleteCouponByIndexAsync(string userName, int index)
        {
            var user = await _couponsService.GetAsyncByUserName(userName);

            if (user == null || index < 0 || index >= user.Coupons.Count)
            {
                return NotFound();
            }

            user.Coupons.RemoveAt(index);
            await _couponsService.UpdateAsync(userName, user);
            return Ok();
        }
    }
}