using Newtonsoft.Json.Linq;
using System.Text;
using Newtonsoft.Json;
using Server_API.Models;
using Mscc.GenerativeAI;
namespace Server_API.Services
{
    public class MLModelsService
    {
        private readonly HttpClient _httpClient;
        private readonly string _mlModelsUrl;
        private readonly string _aiAPIKey;
        public MLModelsService(HttpClient httpClient, IConfiguration configuration)
        {
            _httpClient = httpClient;
            _mlModelsUrl = configuration["MLModelsUrl"];
            _aiAPIKey = configuration["AIApiKey"];
        }
        public async Task<string> GetNER(string input)
        {
            var googleAI = new GoogleAI(apiKey: _aiAPIKey);
            var model = googleAI.GenerativeModel(model: Model.Gemini15Flash);
            var prompt = "I will give you a coupon description (marked with * at the start and at the end). You need to analyze the coupon and return a json object (and nothing else!) with the following fields: title,category,company,coupon_code,description and expiration_date in the format yyyy--MM--dd" + "*" + input + "*";
            var response = await model.GenerateContent(prompt);
            try
            {
                return response.Text;
            }
            catch (Exception ex)
            {
                throw new Exception($"Error recieve respond from Gemini: {ex.Message}");
            }
        }
        public async Task<List<double>> GetEmbedding(string input)
        {
            var url = $"{_mlModelsUrl}embedding";

            // Create a JSON object with the string parameter
            var postData = new JObject { ["input"] = input };
            var content = new StringContent(postData.ToString(), Encoding.UTF8, "application/json");

            HttpResponseMessage response = await _httpClient.PostAsync(url, content);
            if (response.IsSuccessStatusCode)
            {
                string jsonData = await response.Content.ReadAsStringAsync();
                var arrayData = JsonConvert.DeserializeObject<Dictionary<string, List<double>>>(jsonData);
                var embedding_list = arrayData["embedding_list"];
                return embedding_list;
            }
            else
            {
                throw new HttpRequestException($"Error calling MLModels server: {response.StatusCode}");
            }
        }
        public async Task<List<Product>> GetMLRecommendationsModel(List<double> embeddings)
        {
            var url = $"{_mlModelsUrl}MLRecommendationsModel";
            var content = new StringContent(JsonConvert.SerializeObject(embeddings), Encoding.UTF8, "application/json");

            // Send the POST request to Flask
            HttpResponseMessage response = await _httpClient.PostAsync(url, content);
            if (response.IsSuccessStatusCode)
            {
                var jsonData = await response.Content.ReadAsStringAsync();
                var jsonOutput = JsonConvert.DeserializeObject<List<Product>>(jsonData); // Deserialize the JSON response
                return jsonOutput;
            }
            else
            {
                throw new HttpRequestException($"Error calling MLModels server: {response.StatusCode}");
            }
        }
    }
}
