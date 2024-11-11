using System.Net.Http;
using System.Reflection.Metadata;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Mscc.GenerativeAI;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Server_API.Models;
using Server_API.Services;
using ThirdParty.Json.LitJson;


namespace Server_API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class MLModelsController : ControllerBase
    {
        private readonly MLModelsService _mlModelsService;
        public MLModelsController(MLModelsService mlModelsService)
        {
            _mlModelsService = mlModelsService;
        }

        [HttpGet("NER")]
        public async Task<IActionResult> NER([FromQuery] string input)
        {
            try
            {
                var response = await _mlModelsService.GetNER(input);
                var respons_json = JsonConvert.DeserializeObject<Dictionary<string, string>>(response);
                return Ok(respons_json);
            }
            catch (Exception ex) {
            return BadRequest(ex.Message);
            }
        }

        [HttpGet("embedding")]
        public async Task<IActionResult> Embedding([FromQuery] string input)
        {
            try
            {
                var jsonData = await _mlModelsService.GetEmbedding(input);
                return Ok(jsonData);
            }
            catch (HttpRequestException ex)
            {
                return StatusCode(500, ex.Message);
            }
        }
        [HttpPost("MLRecommendationsModel")]
        public async Task<IActionResult> GetSimilarProducts([FromBody] List<double> embedding)
        {
            try
            {
                var products = await _mlModelsService.GetMLRecommendationsModel(embedding);
                return Ok(products);
            }
            catch (HttpRequestException ex)
            {
                return StatusCode(500, ex.Message);
            }

        }
    }
}