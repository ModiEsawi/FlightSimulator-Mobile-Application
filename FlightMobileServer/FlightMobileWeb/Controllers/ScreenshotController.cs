using System;
using System.Net.Http;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;

namespace FlightMobileServer.Controllers
{
    [Route("/screenshot")]
    [ApiController]
    public class ScreenshotController : ControllerBase
    {
        private readonly string ip;
        private readonly int httpPort;
        private readonly HttpClient client;
        public ScreenshotController(IConfiguration configuration)
        {
            this.client = new HttpClient();
            this.ip = configuration["simulator:ip"];
            this.httpPort = int.Parse(configuration["simulator:httpPort"]);
        }

        [HttpGet]
        public async Task<IActionResult> GetScreenshotAsync()
        {
            int timeout = 2500;
            var task = SendGetRequest();
            //wait maximum 2.5 seconds if there is no answer.
            if (await Task.WhenAny(task, Task.Delay(timeout)) == task)
            {
                var result = task.Result; //response.
                if (result != null) //success
                    return File(result, "image/jpeg"); //200 Ok  + the image
                return BadRequest();
            }
            return BadRequest();

        }

        //seng get request to http server , return image (byte array)
        private async Task<byte[]> SendGetRequest()
        {
            try
            {
                string to = "http://" + ip + ":" + httpPort + "/screenshot";
                byte[] response = await client.GetByteArrayAsync(to);
                return response;
            }
            catch (Exception)
            {
                return null;
            }
        }
    }
}