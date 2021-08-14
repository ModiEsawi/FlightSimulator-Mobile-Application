using System.Text.Json.Serialization;
namespace FlightGearServer.Model.Entities
{
    public class Command
    {
        [JsonPropertyName("aileron")]
        public double Aileron { get; set; }

        [JsonPropertyName("rudder")]
        public double Rudder { get; set; }

        [JsonPropertyName("elevator")]
        public double Elevator { get; set; }

        [JsonPropertyName("throttle")]
        public double Throttle { get; set; }

    }
}
