class SensorConfiguration(val config: Configuration) {

}

class Sensors(
    val config: Configuration,
) {
    val sensors: List<SensorConfiguration> by lazy {
        
    }
}