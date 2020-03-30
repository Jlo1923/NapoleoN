package com.naposystems.pepito.utility.emojiManager.categories

import com.naposystems.pepito.model.emojiKeyboard.Emoji
import com.naposystems.pepito.model.emojiKeyboard.EmojiCategory
import com.naposystems.pepito.utility.Constants

class TravelAndPlacesCategory : EmojiCategory() {

    init {
        this.id = Constants.EmojiCategory.TRAVEL_AND_PLACES.category
        this.name = "Travel & Places"
        this.emojiList = arrayListOf(
//            Emoji(1, "Person Rowing Boat", 0x1F6A3),
            Emoji(2, "Map of Japan", 0x1F5FE),
            Emoji(3, "Snow-Capped Mountain", 0x1F3D4),
            Emoji(4, "Mountain", 	0x26F0, 0xFE0F),
            Emoji(5, "Volcano", 0x1F30B),
            Emoji(6, "Mount Fuji", 0x1F5FB),
            Emoji(7, "Camping", 0x1F3D5),
            Emoji(8, "Beach with Umbrella", 0x1F3D6),
            Emoji(9, "Desert", 0x1F3DC),
            Emoji(10, "Desert Island", 0x1F3DD),
            Emoji(11, "National Park", 0x1F3DE),
            Emoji(12, "Stadium", 0x1F3DF),
            Emoji(13, "Classical Building", 0x1F3DB),
            Emoji(14, "Building Construction", 0x1F3D7),
            Emoji(15, "Houses", 0x1F3D8),
            Emoji(16, "Derelict House", 0x1F3DA),
            Emoji(17, "House", 0x1F3E0),
            Emoji(18, "House with Garden", 0x1F3E1),
            Emoji(19, "Office Building", 0x1F3E2),
            Emoji(20, "Japanese Post Office", 0x1F3E3),
            Emoji(21, "Post Office", 0x1F3E4),
            Emoji(22, "Hospital", 0x1F3E5),
            Emoji(23, "Bank", 0x1F3E6),
            Emoji(24, "Hotel", 0x1F3E8),
            Emoji(25, "Love Hotel", 0x1F3E9),
            Emoji(26, "Convenience Store", 0x1F3EA),
            Emoji(27, "School", 0x1F3EB),
            Emoji(28, "Department Store", 0x1F3EC),
            Emoji(29, "Factory", 0x1F3ED),
            Emoji(30, "Japanese Castle", 0x1F3EF),
            Emoji(31, "Castle", 0x1F3F0),
            Emoji(32, "Wedding", 0x1F492),
            Emoji(33, "Tokyo Tower", 0x1F5FC),
            Emoji(34, "Statue of Liberty", 0x1F5FD),
            Emoji(35, "Church", 0x26EA),
            Emoji(36, "Mosque", 0x1F54C),
//            Emoji(37, "Hindu Temple", 0x1F6D5),
            Emoji(38, "Synagogue", 0x1F54D),
            Emoji(39, "Shinto Shrine", 	0x26E9, 0xFE0F),
            Emoji(40, "Kaaba", 0x1F54B),
            Emoji(41, "Fountain", 0x26F2),
            Emoji(42, "Tent", 0x26FA),
            Emoji(43, "Foggy", 0x1F301),
            Emoji(44, "Night with Stars", 0x1F303),
            Emoji(45, "Cityscape", 0x1F3D9),
            Emoji(46, "Sunrise Over Mountains", 0x1F304),
            Emoji(47, "Sunrise", 0x1F305),
            Emoji(48, "Cityscape at Dusk", 0x1F306),
            Emoji(49, "Sunset", 0x1F307),
            Emoji(50, "Bridge at Night", 0x1F309),
            Emoji(51, "Carousel Horse", 0x1F3A0),
            Emoji(52, "Ferris Wheel", 0x1F3A1),
            Emoji(53, "Roller Coaster", 0x1F3A2),
            Emoji(54, "Locomotive", 0x1F682),
            Emoji(55, "Railway Car", 0x1F683),
            Emoji(56, "High-Speed Train", 0x1F684),
            Emoji(57, "Bullet Train", 0x1F685),
            Emoji(58, "Train", 0x1F686),
            Emoji(59, "Metro", 0x1F687),
            Emoji(60, "Light Rail", 0x1F688),
            Emoji(61, "Station", 0x1F689),
            Emoji(62, "Tram", 0x1F68A),
            Emoji(63, "Monorail", 0x1F69D),
            Emoji(64, "Mountain Railway", 0x1F69E),
            Emoji(65, "Tram Car", 0x1F68B),
            Emoji(66, "Bus", 0x1F68C),
            Emoji(67, "Oncoming Bus", 0x1F68D),
            Emoji(68, "Trolleybus", 0x1F68E),
            Emoji(69, "Minibus", 0x1F690),
            Emoji(70, "Ambulance", 0x1F691),
            Emoji(71, "Fire Engine", 0x1F692),
            Emoji(72, "Police Car", 0x1F693),
            Emoji(73, "Oncoming Police Car", 0x1F694),
            Emoji(74, "Taxi", 0x1F695),
            Emoji(75, "Oncoming Taxi", 0x1F696),
            Emoji(76, "Automobile", 0x1F697),
            Emoji(77, "Oncoming Automobile", 0x1F698),
            Emoji(78, "Delivery Truck", 0x1F69A),
            Emoji(79, "Articulated Lorry", 0x1F69B),
            Emoji(80, "Tractor", 0x1F69C),
            Emoji(81, "Racing Car", 0x1F3CE),
            Emoji(82, "Motorcycle", 0x1F3CD),
            Emoji(83, "Motor Scooter", 0x1F6F5),
//            Emoji(84, "Auto Rickshaw", 0x1F6FA),
            Emoji(85, "Bicycle", 0x1F6B2),
            Emoji(86, "Kick Scooter", 0x1F6F4),
            Emoji(87, "Bus Stop", 0x1F68F),
            Emoji(88, "Railway Track", 0x1F6E4),
            Emoji(89, "Fuel Pump", 0x26FD),
            Emoji(90, "Police Car Light", 0x1F6A8),
            Emoji(91, "Horizontal Traffic Light", 0x1F6A5),
            Emoji(92, "Vertical Traffic Light", 0x1F6A6),
            Emoji(93, "Construction", 0x1F6A7),
            Emoji(94, "Anchor", 0x2693),
            Emoji(95, "Sailboat", 0x26F5),
            Emoji(96, "Speedboat", 0x1F6A4),
            Emoji(97, "Passenger Ship", 0x1F6F3, 0xFE0F),
            Emoji(98, "Ferry", 0x26F4, 0xFE0F),
            Emoji(99, "Motor Boat", 0x1F6E5, 0xFE0F),
            Emoji(100, "Ship", 0x1F6A2),
            Emoji(101, "Airplane", 0x2708, 0xFE0F),
            Emoji(102, "Small Airplane", 0x1F6E9, 0xFE0F),
            Emoji(103, "Airplane Departure", 0x1F6EB),
            Emoji(104, "Airplane Arrival", 0x1F6EC),
            Emoji(105, "Parachute", 0x1FA82),
            Emoji(106, "Seat", 0x1F4BA),
            Emoji(107, "Helicopter", 0x1F681),
            Emoji(108, "Suspension Railway", 0x1F69F),
            Emoji(109, "Mountain Cableway", 0x1F6A0),
            Emoji(110, "Aerial Tramway", 0x1F6A1),
            Emoji(111, "Satellite", 0x1F6F0, 0xFE0F),
            Emoji(112, "Rocket", 0x1F680),
            Emoji(113, "Flying Saucer", 0x1F6F8),
//            Emoji(114, "Ringed Planet", 0x1FA90),
            Emoji(115, "Shooting Star", 0x1F320),
            Emoji(116, "Milky Way", 0x1F30C),
            Emoji(117, "Umbrella on Ground", 0x26F1, 0xFE0F),
            Emoji(118, "Fireworks", 0x1F386),
            Emoji(119, "Sparkler", 0x1F387),
            Emoji(120, "Moon Viewing Ceremony", 0x1F391),
            Emoji(121, "Yen Banknote", 0x1F4B4),
            Emoji(122, "Dollar Banknote", 0x1F4B5),
            Emoji(123, "Euro Banknote", 0x1F4B6),
            Emoji(124, "Pound Banknote", 0x1F4B7),
            Emoji(125, "Moai", 0x1F5FF),
            Emoji(126, "Passport Control", 0x1F6C2),
            Emoji(127, "Customs", 0x1F6C3),
            Emoji(128, "Baggage Claim", 0x1F6C4),
            Emoji(129, "Left Luggage", 0x1F6C5)
        )
    }
}