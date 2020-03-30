package com.naposystems.pepito.utility.emojiManager.categories

import com.naposystems.pepito.model.emojiKeyboard.Emoji
import com.naposystems.pepito.model.emojiKeyboard.EmojiCategory
import com.naposystems.pepito.utility.Constants

class AnimalsAndNatureCategory : EmojiCategory() {

    init {
        this.id = Constants.EmojiCategory.ANIMALS_AND_NATURE.category
        this.name = "Animals & Nature"
        this.emojiList = arrayListOf(
            Emoji(1, "See-No-Evil Monkey", 0x1F648),
            Emoji(2, "Hear-No-Evil Monkey", 0x1F649),
            Emoji(3, "Speak-No-Evil Monkey", 0x1F64A),
            Emoji(4, "Collision", 0x1F4A5),
            Emoji(5, "Dizzy", 0x1F4AB),
            Emoji(6, "Sweat Droplets", 0x1F4A6),
            Emoji(7, "Dashing Away", 0x1F4A8),
            Emoji(8, "Monkey Face", 0x1F435),
            Emoji(9, "Monkey", 0x1F412),
            Emoji(10, "Gorilla", 0x1F98D),
//            Emoji(11, "Orangutan", 0x1F9A7),
            Emoji(12, "Dog Face", 0x1F436),
            Emoji(13, "Dog", 0x1F415),
//            Emoji(14, "Guide Dog", 0x1F9AE),
//            Emoji(15, "Service Dog", 	0x1F415, 0x200D, 0x1F9BA),
            Emoji(16, "Poodle", 0x1F429),
            Emoji(17, "Wolf", 0x1F43A),
            Emoji(18, "Fox", 0x1F98A),
            Emoji(19, "Raccoon", 0x1F99D),
            Emoji(20, "Cat Face", 0x1F431),
            Emoji(21, "Cat", 0x1F408),
            Emoji(22, "Lion", 0x1F981),
            Emoji(23, "Tiger Face", 0x1F42F),
            Emoji(24, "Tiger", 0x1F405),
            Emoji(25, "Leopard", 0x1F406),
            Emoji(26, "Horse Face", 0x1F434),
            Emoji(27, "Horse", 0x1F40E),
            Emoji(28, "Unicorn", 0x1F984),
            Emoji(29, "Zebra", 0x1F993),
            Emoji(30, "Deer", 0x1F98C),
            Emoji(31, "Cow Face", 0x1F42E),
            Emoji(32, "Ox", 0x1F402),
            Emoji(33, "Water Buffalo", 0x1F403),
            Emoji(34, "Cow", 0x1F404),
            Emoji(35, "Pig Face", 0x1F437),
            Emoji(36, "Pig", 0x1F416),
            Emoji(37, "Boar", 0x1F417),
            Emoji(38, "Pig Nose", 0x1F43D),
            Emoji(39, "Ram", 0x1F40F),
            Emoji(40, "Ewe", 0x1F411),
            Emoji(41, "Goat", 0x1F410),
            Emoji(42, "Camel", 0x1F42A),
            Emoji(43, "Two-Hump Camel", 0x1F42B),
            Emoji(44, "Llama", 0x1F999),
            Emoji(45, "Giraffe", 0x1F992),
            Emoji(46, "Elephant", 0x1F418),
            Emoji(47, "Rhinoceros", 0x1F98F),
            Emoji(48, "Hippopotamus", 0x1F99B),
            Emoji(49, "Mouse Face", 0x1F42D),
            Emoji(50, "Mouse", 0x1F401),
            Emoji(51, "Rat", 0x1F400),
            Emoji(52, "Hamster", 0x1F439),
            Emoji(53, "Rabbit Face", 0x1F430),
            Emoji(54, "Rabbit", 0x1F407),
            Emoji(55, "Chipmunk", 0x1F43F),
            Emoji(56, "Hedgehog", 0x1F994),
            Emoji(57, "Bat", 0x1F987),
            Emoji(58, "Bear", 0x1F43B),
            Emoji(59, "Koala", 0x1F428),
            Emoji(60, "Panda", 0x1F43C),
//            Emoji(61, "Sloth", 0x1F9A5),
//            Emoji(62, "Otter", 0x1F9A6),
//            Emoji(63, "Skunk", 0x1F9A8),
            Emoji(64, "Kangaroo", 0x1F998),
            Emoji(65, "Badger", 0x1F9A1),
            Emoji(66, "Paw Prints", 0x1F43E),
            Emoji(67, "Turkey", 0x1F983),
            Emoji(68, "Chicken", 0x1F414),
            Emoji(69, "Rooster", 0x1F413),
            Emoji(70, "Hatching Chick", 0x1F423),
            Emoji(71, "Baby Chick", 0x1F424),
            Emoji(72, "Front-Facing Baby Chick", 0x1F425),
            Emoji(73, "Bird", 0x1F426),
            Emoji(74, "Penguin", 0x1F427),
            Emoji(75, "Dove", 0x1F54A),
            Emoji(76, "Eagle", 0x1F985),
            Emoji(77, "Duck", 0x1F986),
            Emoji(78, "Swan", 0x1F9A2),
            Emoji(79, "Owl", 0x1F989),
//            Emoji(80, "Flamingo", 0x1F9A9),
            Emoji(81, "Peacock", 0x1F99A),
            Emoji(82, "Parrot", 0x1F99C),
            Emoji(83, "Frog", 0x1F438),
            Emoji(84, "Crocodile", 0x1F40A),
            Emoji(85, "Turtle", 0x1F422),
            Emoji(86, "Lizard", 0x1F98E),
            Emoji(87, "Snake", 0x1F40D),
            Emoji(88, "Dragon Face", 0x1F432),
            Emoji(89, "Dragon", 0x1F409),
            Emoji(90, "Sauropod", 0x1F995),
            Emoji(91, "T-Rex", 0x1F996),
            Emoji(92, "Spouting Whale", 0x1F433),
            Emoji(93, "Whale", 0x1F40B),
            Emoji(94, "Dolphin", 0x1F42C),
            Emoji(95, "Fish", 0x1F41F),
            Emoji(96, "Tropical Fish", 0x1F420),
            Emoji(97, "Blowfish", 0x1F421),
            Emoji(98, "Shark", 0x1F988),
            Emoji(99, "Octopus", 0x1F419),
            Emoji(100, "Spiral Shell", 0x1F41A),
            Emoji(101, "Snail", 0x1F40C),
            Emoji(102, "Butterfly", 0x1F98B),
            Emoji(103, "Bug", 0x1F41B),
            Emoji(104, "Ant", 0x1F41C),
            Emoji(105, "Honeybee", 0x1F41D),
            Emoji(106, "Lady Beetle", 0x1F41E),
            Emoji(107, "Cricket", 0x1F997),
            Emoji(108, "Spider", 0x1F577),
            Emoji(109, "Spider Web", 0x1F578),
            Emoji(110, "Scorpion", 0x1F982),
            Emoji(111, "Mosquito", 0x1F99F),
            Emoji(112, "Microbe", 0x1F9A0),
            Emoji(113, "Bouquet", 0x1F490),
            Emoji(114, "Cherry Blossom", 0x1F338),
            Emoji(115, "White Flower", 0x1F4AE),
            Emoji(116, "Rosette", 0x1F3F5),
            Emoji(117, "Rose", 0x1F339),
            Emoji(118, "Wilted Flower", 0x1F940),
            Emoji(119, "Hibiscus", 0x1F33A),
            Emoji(120, "Sunflower", 0x1F33B),
            Emoji(121, "Blossom", 0x1F33C),
            Emoji(122, "Tulip", 0x1F337),
            Emoji(123, "Seedling", 0x1F331),
            Emoji(124, "Evergreen Tree", 0x1F332),
            Emoji(125, "Deciduous Tree", 0x1F333),
            Emoji(126, "Palm Tree", 0x1F334),
            Emoji(127, "Cactus", 0x1F335),
            Emoji(128, "Sheaf of Rice", 0x1F33E),
            Emoji(129, "Herb", 0x1F33F),
            Emoji(130, "Shamrock", 0x2618),
            Emoji(131, "Four Leaf Clover", 0x1F340),
            Emoji(132, "Maple Leaf", 0x1F341),
            Emoji(133, "Fallen Leaf", 0x1F342),
            Emoji(134, "Leaf Fluttering in Wind", 0x1F343),
            Emoji(135, "Mushroom", 0x1F344),
            Emoji(136, "Chestnut", 0x1F330),
            Emoji(137, "Crab", 0x1F980),
            Emoji(138, "Lobster", 0x1F99E),
            Emoji(139, "Shrimp", 0x1F990),
            Emoji(140, "Squid", 0x1F991),
            Emoji(141, "Globe Showing Europe-Africa", 0x1F30D),
            Emoji(142, "Globe Showing Americas", 0x1F30E),
            Emoji(143, "Globe Showing Asia-Australia", 0x1F30F),
            Emoji(144, "Globe with Meridians", 0x1F310),
            Emoji(145, "New Moon", 0x1F311),
            Emoji(146, "Waxing Crescent Moon", 0x1F312),
            Emoji(147, "First Quarter Moon", 0x1F313),
            Emoji(148, "Waxing Gibbous Moon", 0x1F314),
            Emoji(149, "Full Moon", 0x1F315),
            Emoji(150, "Waning Gibbous Moon", 0x1F316),
            Emoji(151, "Last Quarter Moon", 0x1F317),
            Emoji(152, "Waning Crescent Moon", 0x1F318),
            Emoji(153, "Crescent Moon", 0x1F319),
            Emoji(154, "New Moon Face", 0x1F31A),
            Emoji(155, "First Quarter Moon Face", 0x1F31B),
            Emoji(156, "Last Quarter Moon Face", 0x1F31C),
            Emoji(157, "Sun", 0x2600),
            Emoji(158, "Full Moon Face", 0x1F31D),
            Emoji(159, "Sun with Face", 0x1F31E),
            Emoji(160, "Star", 0x2B50),
            Emoji(161, "Glowing Star", 0x1F31F),
            Emoji(162, "Shooting Star", 0x1F320),
            Emoji(163, "Cloud", 0x2601),
            Emoji(164, "Sun Behind Cloud", 0x26C5),
            Emoji(165, "Cloud with Lightning and Rain", 0x26C8, 0xFE0F),
            Emoji(166, "Sun Behind Small Cloud", 0x1F324),
            Emoji(167, "Sun Behind Large Cloud", 0x1F325),
            Emoji(168, "Sun Behind Rain Cloud", 0x1F326),
            Emoji(169, "Cloud with Rain", 0x1F327),
            Emoji(170, "Cloud with Snow", 0x1F328),
            Emoji(171, "Cloud with Lightning", 0x1F329),
            Emoji(172, "Tornado", 0x1F32A),
            Emoji(173, "Fog", 0x1F32B),
            Emoji(174, "Wind Face", 0x1F32C),
            Emoji(175, "Rainbow", 0x1F308),
            Emoji(176, "Umbrella", 0x2602, 0xFE0F),
            Emoji(177, "Umbrella with Rain Drops", 0x2614),
            Emoji(178, "High Voltage", 0x26A1),
            Emoji(179, "Snowflake", 0x2744),
            Emoji(180, "Snowman", 	0x2603, 0xFE0F),
            Emoji(181, "Snowman Without Snow", 0x26C4),
            Emoji(182, "Comet", 0x2604, 0xFE0F),
            Emoji(183, "Fire", 0x1F525),
            Emoji(184, "Droplet", 0x1F4A7),
            Emoji(185, "Water Wave", 0x1F30A),
            Emoji(186, "Christmas Tree", 0x1F384),
            Emoji(187, "Sparkles", 0x2728),
            Emoji(188, "Tanabata Tree", 0x1F38B),
            Emoji(189, "Pine Decoration", 0x1F38D)
        )
    }
}