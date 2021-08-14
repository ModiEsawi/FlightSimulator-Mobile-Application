using FlightGearServer.Model;
using FlightGearServer.Model.Entities;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;

namespace FlightMobileServer.Controllers
{
    [Route("api/command")]
    [ApiController]
    public class CommandController : Controller
    {
        private readonly string ip;
        private readonly int port;
        private ISimulatorRepository simulatorRepo;

        public CommandController(IConfiguration configuration , ISimulatorRepository simulatorRepo)
        {
            this.ip = configuration["simulator:ip"];
            this.port = int.Parse(configuration["simulator:port"]);
            this.simulatorRepo = simulatorRepo;
        }
        [HttpPost]
        public IActionResult SendCommand([FromBody] Command command)
        {
            if (!simulatorRepo.IsConnected()) 
            {
                if (!simulatorRepo.TryConnect(ip, port))
                    return BadRequest(); //server offline
            }
            bool success = this.simulatorRepo.SendCommand(command);
            if (success) //vars upadated
                return Ok();
            return NotFound(); //not all vars updated in simulator
        }
    }


}