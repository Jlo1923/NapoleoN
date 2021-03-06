package com.naposystems.napoleonchat.utility.emojiManager.categories

import com.naposystems.napoleonchat.model.emojiKeyboard.Emoji
import com.naposystems.napoleonchat.model.emojiKeyboard.EmojiCategory
import com.naposystems.napoleonchat.utility.Constants
import java.io.Serializable

class SmileysAndPeopleCategory : EmojiCategory(), Serializable {

    init {
        this.id = Constants.EmojiCategory.SMILES_AND_PEOPLE.category
        this.name = "Smileys & People"
        this.emojiList = arrayListOf(
            Emoji(1, "Grinning Face", 0x1F600),
            Emoji(2, "Grinning Face with Big Eyes", 0x1F603),
            Emoji(3, "Grinning Face with Smiling Eyes", 0x1F604),
            Emoji(4, "Beaming Face with Smiling Eyes", 0x1F601),
            Emoji(5, "Grinning Squinting Face", 0x1F606),
            Emoji(6, "Grinning Face with Sweat", 0x1F605),
            Emoji(7, "Rolling on the Floor Laughing", 0x1F923),
            Emoji(8, "Face with Tears of Joy", 0x1F602),
            Emoji(9, "Slightly Smiling Face", 0x1F642),
            Emoji(10, "Upside-Down Face", 0x1F643),
            Emoji(11, "Winking Face", 0x1F609),
            Emoji(12, "Smiling Face with Smiling Eyes", 0x1F60A),
            Emoji(13, "Smiling Face with Halo", 0x1F607),
            Emoji(14, "Smiling Face with Hearts", 0x1F970),
            Emoji(15, "Smiling Face with Heart-Eyes", 0x1F60D),
            Emoji(16, "Star-Struck", 0x1F929),
            Emoji(17, "Face Blowing a Kiss", 0x1F618),
            Emoji(18, "Kissing Face", 0x1F617),
            Emoji(19, "Smiling Face", 0x263A),
            Emoji(20, "Kissing Face with Closed Eyes", 0x1F61A),
            Emoji(21, "Kissing Face with Smiling Eyes", 0x1F619),
            Emoji(22, "Face Savoring Food", 0x1F60B),
            Emoji(23, "Face with Tongue", 0x1F61B),
            Emoji(24, "Winking Face with Tongue", 0x1F61C),
            Emoji(25, "Zany Face", 0x1F92A),
            Emoji(26, "Squinting Face with Tongue", 0x1F61D),
            Emoji(27, "Money-Mouth Face", 0x1F911),
            Emoji(28, "Hugging Face", 0x1F917),
            Emoji(29, "Face with Hand Over Mouth", 0x1F92D),
            Emoji(30, "Shushing Face", 0x1F92B),
            Emoji(31, "Thinking Face", 0x1F914),
            Emoji(32, "Zipper-Mouth Face", 0x1F910),
            Emoji(33, "Face with Raised Eyebrow", 0x1F928),
            Emoji(34, "Neutral Face", 0x1F610),
            Emoji(35, "Expressionless Face", 0x1F611),
            Emoji(36, "Face Without Mouth", 0x1F636),
            Emoji(37, "Smirking Face", 0x1F60F),
            Emoji(38, "Unamused Face", 0x1F612),
            Emoji(39, "Face with Rolling Eyes", 0x1F644),
            Emoji(40, "Grimacing Face", 0x1F62C),
            Emoji(41, "Lying Face", 0x1F925),
            Emoji(42, "Relieved Face", 0x1F60C),
            Emoji(43, "Pensive Face", 0x1F614),
            Emoji(44, "Sleepy Face", 0x1F62A),
            Emoji(45, "Drooling Face", 0x1F924),
            Emoji(46, "Sleeping Face", 0x1F634),
            Emoji(47, "Face with Medical Mask", 0x1F637),
            Emoji(48, "Face with Thermometer", 0x1F912),
            Emoji(49, "Face with Head-Bandage", 0x1F915),
            Emoji(50, "Nauseated Face", 0x1F922),
            Emoji(51, "Face Vomiting", 0x1F92E),
            Emoji(52, "Sneezing Face", 0x1F927),
            Emoji(53, "Hot Face", 0x1F975),
            Emoji(54, "Cold Face", 0x1F976),
            Emoji(55, "Woozy Face", 0x1F974),
            Emoji(56, "Dizzy Face", 0x1F635),
            Emoji(57, "Exploding Head", 0x1F92F),
            Emoji(58, "Cowboy Hat Face", 0x1F920),
            Emoji(59, "Partying Face", 0x1F973),
            Emoji(60, "Smiling Face with Sunglasses", 0x1F60E),
            Emoji(61, "Nerd Face", 0x1F913),
            Emoji(62, "Face with Monocle", 0x1F9D0),
            Emoji(63, "Confused Face", 0x1F615),
            Emoji(64, "Worried Face", 0x1F61F),
            Emoji(65, "Slightly Frowning Face", 0x1F641),
            Emoji(66, "Frowning Face", 0x2639, 0xFE0F),
            Emoji(67, "Face with Open Mouth", 0x1F62E),
            Emoji(68, "Hushed Face", 0x1F62F),
            Emoji(69, "Astonished Face", 0x1F632),
            Emoji(70, "Flushed Face", 0x1F633),
            Emoji(71, "Pleading Face", 0x1F97A),
            Emoji(72, "Frowning Face with Open Mouth", 0x1F626),
            Emoji(73, "Anguished Face", 0x1F627),
            Emoji(74, "Fearful Face", 0x1F628),
            Emoji(75, "Anxious Face with Sweat", 0x1F630),
            Emoji(76, "Sad but Relieved Face", 0x1F625),
            Emoji(77, "Crying Face", 0x1F622),
            Emoji(78, "Loudly Crying Face", 0x1F62D),
            Emoji(79, "Face Screaming in Fear", 0x1F631),
            Emoji(80, "Confounded Face", 0x1F616),
            Emoji(81, "Persevering Face", 0x1F623),
            Emoji(82, "Disappointed Face", 0x1F61E),
            Emoji(83, "Downcast Face with Sweat", 0x1F613),
            Emoji(84, "Weary Face", 0x1F629),
            Emoji(85, "Tired Face", 0x1F62B),
//            Emoji(86, "Yawning Face", 0x1F971),
            Emoji(87, "Face with Steam From Nose", 0x1F624),
            Emoji(88, "Pouting Face", 0x1F621),
            Emoji(89, "Angry Face", 0x1F620),
            Emoji(90, "Face with Symbols on Mouth", 0x1F92C),
            Emoji(91, "Smiling Face with Horns", 0x1F608),
            Emoji(92, "Angry Face with Horns", 0x1F47F),
            Emoji(93, "Skull", 0x1F480),
            Emoji(94, "Skull and Crossbones", 0x2620, 0xFE0F),
            Emoji(95, "Pile of Poo", 0x1F4A9),
            Emoji(96, "Clown Face", 0x1F921),
            Emoji(97, "Ogre", 0x1F479),
            Emoji(98, "Goblin", 0x1F47A),
            Emoji(99, "Ghost", 0x1F47B),
            Emoji(100, "Alien", 0x1F47D),
            Emoji(101, "Alien Monster", 0x1F47E),
            Emoji(102, "Robot", 0x1F916),
            Emoji(103, "Grinning Cat", 0x1F63A),
            Emoji(104, "Grinning Cat with Smiling Eyes", 0x1F638),
            Emoji(105, "Cat with Tears of Joy", 0x1F639),
            Emoji(106, "Smiling Cat with Heart-Eyes", 0x1F63B),
            Emoji(107, "Cat with Wry Smile", 0x1F63C),
            Emoji(108, "Kissing Cat", 0x1F63D),
            Emoji(109, "Weary Cat", 0x1F640),
            Emoji(110, "Crying Cat", 0x1F63F),
            Emoji(111, "Pouting Cat", 0x1F63E),
            Emoji(112, "Kiss Mark", 0x1F48B),
            Emoji(113, "Waving Hand", 0x1F44B),
            Emoji(114, "Raised Back of Hand", 0x1F91A),
            Emoji(115, "Hand with Fingers Splayed", 0x1F590),
            Emoji(116, "Raised Hand", 0x270B),
            Emoji(117, "Vulcan Salute", 0x1F596),
            Emoji(118, "OK Hand", 0x1F44C),
//            Emoji(119, "Pinching Hand", 0x1F90F),
            Emoji(120, "Victory Hand", 0x270C),
            Emoji(121, "Crossed Fingers", 0x1F91E),
            Emoji(122, "Love-You Gesture", 0x1F91F),
            Emoji(123, "Sign of the Horns", 0x1F918),
            Emoji(124, "Call Me Hand", 0x1F919),
            Emoji(125, "Backhand Index Pointing Left", 0x1F448),
            Emoji(126, "Backhand Index Pointing Right", 0x1F449),
            Emoji(127, "Backhand Index Pointing Up", 0x1F446),
            Emoji(128, "Middle Finger", 0x1F595),
            Emoji(129, "Backhand Index Pointing Down", 0x1F447),
            Emoji(130, "Index Pointing Up", 0x261D),
            Emoji(131, "Thumbs Up", 0x1F44D),
            Emoji(132, "Thumbs Down", 0x1F44E),
            Emoji(133, "Raised Fist", 0x270A),
            Emoji(134, "Oncoming Fist", 0x1F44A),
            Emoji(135, "Left-Facing Fist", 0x1F91B),
            Emoji(136, "Right-Facing Fist", 0x1F91C),
            Emoji(137, "Clapping Hands", 0x1F44F),
            Emoji(138, "Raising Hands", 0x1F64C),
            Emoji(139, "Open Hands", 0x1F450),
            Emoji(140, "Palms Up Together", 0x1F932),
            Emoji(141, "Handshake", 0x1F91D),
            Emoji(142, "Folded Hands", 0x1F64F),
            Emoji(143, "Writing Hand", 0x270D, 0xFE0F),
            Emoji(144, "Nail Polish", 0x1F485),
            Emoji(145, "Selfie", 0x1F933),
            Emoji(146, "Flexed Biceps", 0x1F4AA),
//            Emoji(147, "Mechanical Arm", 0x1F9BE),
////            Emoji(148, "Mechanical Leg", 0x1F9BF),
            Emoji(149, "Leg", 0x1F9B5),
            Emoji(150, "Foot", 0x1F9B6),
            Emoji(151, "Ear", 0x1F442),
//            Emoji(152, "Ear with Hearing Aid", 0x1F9BB),
            Emoji(153, "Nose", 0x1F443),
            Emoji(154, "Brain", 0x1F9E0),
            Emoji(155, "Tooth", 0x1F9B7),
            Emoji(156, "Bone", 0x1F9B4),
            Emoji(157, "Eyes", 0x1F440),
            Emoji(158, "Eye", 0x1F441),
            Emoji(159, "Tongue", 0x1F445),
            Emoji(160, "Mouth", 0x1F444),
            Emoji(161, "Baby", 0x1F476),
            Emoji(162, "Child", 0x1F9D2),
            Emoji(163, "Boy", 0x1F466),
            Emoji(164, "Girl", 0x1F467),
            Emoji(165, "Person", 0x1F9D1),
            Emoji(166, "Person: Blond Hair", 0x1F471),
            Emoji(167, "Man", 0x1F468),
            Emoji(168, "Man: Beard", 0x1F9D4),
            Emoji(169, "Man: Red Hair", 0x1F468, 0x200D, 0x1F9B0),
            Emoji(170, "Man: Curly Hair", 0x1F468, 0x200D, 0x1F9B1),
            Emoji(171, "Man: White Hair", 0x1F468, 0x200D, 0x1F9B3),
            Emoji(172, "Man: Bald", 0x1F468, 0x200D, 0x1F9B2),
            Emoji(173, "Woman", 0x1F469),
            Emoji(174, "Woman: Red Hair", 0x1F469, 0x200D, 0x1F9B0),
//            Emoji(175, "Person: Red Hair", 0x1F9D1, 0x200D, 0x1F9B0),
            Emoji(176, "Woman: Curly Hair", 0x1F469, 0x200D, 0x1F9B1),
//            Emoji(177, "Person: Curly Hair", 0x1F9D1, 0x200D, 0x1F9B1),
            Emoji(178, "Woman: White Hair", 0x1F469, 0x200D, 0x1F9B3),
//            Emoji(179, "Person: White Hair", 0x1F9D1, 0x200D, 0x1F9B3),
            Emoji(180, "Woman: Bald", 0x1F469, 0x200D, 0x1F9B2),
//            Emoji(181, "Person: Bald", 0x1F9D1, 0x200D, 0x1F9B2),
            Emoji(182, "Woman: Blond Hair", 0x1F471, 0x200D, 0x2640, 0xFE0F),
            Emoji(183, "Man: Blond Hair", 0x1F471, 0x200D, 0x2642, 0xFE0F),
            Emoji(184, "Older Person", 0x1F9D3),
            Emoji(185, "Old Man", 0x1F474),
            Emoji(186, "Old Woman", 0x1F475),
            Emoji(187, "Person Frowning", 0x1F64D),
            Emoji(188, "Man Frowning", 0x1F64D, 0x200D, 0x2642, 0xFE0F),
            Emoji(189, "Woman Frowning", 0x1F64D, 0x200D, 0x2640, 0xFE0F),
            Emoji(190, "Person Pouting", 0x1F64E),
            Emoji(191, "Man Pouting", 0x1F64E, 0x200D, 0x2642, 0xFE0F),
            Emoji(192, "Woman Pouting", 0x1F64E, 0x200D, 0x2640, 0xFE0F),
            Emoji(193, "Person Gesturing No", 0x1F645),
            Emoji(194, "Man Gesturing No", 0x1F645, 0x200D, 0x2642, 0xFE0F),
            Emoji(195, "Woman Gesturing No", 0x1F645, 0x200D, 0x2640, 0xFE0F),
            Emoji(196, "Person Gesturing OK", 0x1F646),
            Emoji(197, "Man Gesturing OK", 0x1F646, 0x200D, 0x2642, 0xFE0F),
            Emoji(198, "Woman Gesturing OK", 0x1F646, 0x200D, 0x2640, 0xFE0F),
            Emoji(199, "Person Tipping Hand", 0x1F481),
            Emoji(200, "Man Tipping Hand", 0x1F481, 0x200D, 0x2642, 0xFE0F),
            Emoji(201, "Woman Tipping Hand", 0x1F481, 0x200D, 0x2640, 0xFE0F),
            Emoji(202, "Person Raising Hand", 0x1F64B),
            Emoji(203, "Man Raising Hand", 0x1F64B, 0x200D, 0x2642, 0xFE0F),
            Emoji(204, "Woman Raising Hand", 0x1F64B, 0x200D, 0x2640, 0xFE0F),
//            Emoji(205, "Deaf Person", 0x1F9CF),
//            Emoji(206, "Deaf Man", 0x1F9CF, 0x200D, 0x2642, 0xFE0F),
//            Emoji(207, "Deaf Woman", 0x1F9CF, 0x200D, 0x2640, 0xFE0F),
//            Emoji(208, "Person Bowing", 0x1F647),
            Emoji(209, "Man Bowing", 0x1F647, 0x200D, 0x2642, 0xFE0F),
            Emoji(210, "Woman Bowing", 0x1F647, 0x200D, 0x2640, 0xFE0F),
//            Emoji(211, "Person Facepalming", 0x1F926),
            Emoji(212, "Man Facepalming", 0x1F926, 0x200D, 0x2642, 0xFE0F),
            Emoji(213, "Woman Facepalming", 0x1F926, 0x200D, 0x2640, 0xFE0F),
//            Emoji(214, "Person Shrugging", 0x1F937),
            Emoji(215, "Man Shrugging", 0x1F937, 0x200D, 0x2642, 0xFE0F),
            Emoji(216, "Woman Shrugging", 0x1F937, 0x200D, 0x2640, 0xFE0F),
//            Emoji(217, "Health Worker", 0x1F9D1, 0x200D, 0x2695, 0xFE0F),
            Emoji(218, "Man Health Worker", 0x1F468, 0x200D, 0x2695, 0xFE0F),
            Emoji(219, "Woman Health Worker", 0x1F469, 0x200D, 0x2695, 0xFE0F),
//            Emoji(220, "Student", 0x1F9D1, 0x200D, 0x1F393),
            Emoji(221, "Man Student", 0x1F468, 0x200D, 0x1F393),
            Emoji(222, "Woman Student", 0x1F469, 0x200D, 0x1F393),
//            Emoji(223, "Teacher", 0x1F9D1, 0x200D, 0x1F3EB),
            Emoji(224, "Man Teacher", 0x1F468, 0x200D, 0x1F3EB),
            Emoji(225, "Woman Teacher", 0x1F469, 0x200D, 0x1F3EB),
//            Emoji(226, "Judge", 0x1F9D1, 0x200D, 0x2696, 0xFE0F),
            Emoji(227, "Man Judge", 0x1F468, 0x200D, 0x2696, 0xFE0F),
            Emoji(228, "Woman Judge", 0x1F469, 0x200D, 0x2696, 0xFE0F),
//            Emoji(229, "Farmer", 0x1F9D1, 0x200D, 0x1F33E),
            Emoji(230, "Man Farmer", 0x1F468, 0x200D, 0x1F33E),
            Emoji(231, "Woman Farmer", 0x1F469, 0x200D, 0x1F33E),
//            Emoji(232, "Cook", 0x1F9D1, 0x200D, 0x1F373),
            Emoji(233, "Man Cook", 0x1F468, 0x200D, 0x1F373),
            Emoji(234, "Woman Cook", 0x1F469, 0x200D, 0x1F373),
//            Emoji(235, "Mechanic", 0x1F9D1, 0x200D, 0x1F527),
            Emoji(236, "Man Mechanic", 0x1F468, 0x200D, 0x1F527),
            Emoji(237, "Woman Mechanic", 0x1F469, 0x200D, 0x1F527),
//            Emoji(238, "Factory Worker", 0x1F9D1, 0x200D, 0x1F3ED),
            Emoji(239, "Man Factory Worker", 0x1F468, 0x200D, 0x1F3ED),
            Emoji(240, "Woman Factory Worker", 0x1F469, 0x200D, 0x1F3ED),
//            Emoji(241, "Office Worker", 0x1F9D1, 0x200D, 0x1F4BC),
            Emoji(242, "Man Office Worker", 0x1F468, 0x200D, 0x1F4BC),
            Emoji(243, "Woman Office Worker", 0x1F469, 0x200D, 0x1F4BC),
//            Emoji(244, "Scientist", 0x1F9D1, 0x200D, 0x1F52C),
            Emoji(245, "Man Scientist", 0x1F468, 0x200D, 0x1F52C),
            Emoji(246, "Woman Scientist", 0x1F469, 0x200D, 0x1F52C),
//            Emoji(247, "Technologist", 0x1F9D1, 0x200D, 0x1F4BB),
            Emoji(248, "Man Technologist", 0x1F468, 0x200D, 0x1F4BB),
            Emoji(249, "Woman Technologist", 0x1F469, 0x200D, 0x1F4BB),
//            Emoji(250, "Singer", 0x1F9D1, 0x200D, 0x1F3A4),
            Emoji(251, "Man Singer", 0x1F468, 0x200D, 0x1F3A4),
            Emoji(252, "Woman Singer", 0x1F469, 0x200D, 0x1F3A4),
//            Emoji(253, "Artist", 0x1F9D1, 0x200D, 0x1F3A8),
            Emoji(254, "Man Artist", 0x1F468, 0x200D, 0x1F3A8),
            Emoji(255, "Woman Artist", 0x1F469, 0x200D, 0x1F3A8),
//            Emoji(256, "Pilot", 0x1F9D1, 0x200D, 0x2708, 0xFE0F),
            Emoji(257, "Man Pilot", 0x1F468, 0x200D, 0x2708, 0xFE0F),
            Emoji(258, "Woman Pilot", 0x1F469, 0x200D, 0x2708, 0xFE0F),
//            Emoji(259, "Astronaut", 0x1F9D1, 0x200D, 0x1F680),
            Emoji(260, "Man Astronaut", 0x1F468, 0x200D, 0x1F680),
            Emoji(261, "Woman Astronaut", 0x1F469, 0x200D, 0x1F680),
//            Emoji(262, "Firefighter", 0x1F9D1, 0x200D, 0x1F692),
            Emoji(263, "Man Firefighter", 0x1F468, 0x200D, 0x1F692),
            Emoji(264, "Woman Firefighter", 0x1F469, 0x200D, 0x1F692),
//            Emoji(265, "Police Officer", 0x1F46E),
            Emoji(266, "Man Police Officer", 0x1F46E, 0x200D, 0x2642, 0xFE0F),
            Emoji(267, "Woman Police Officer", 0x1F46E, 0x200D, 0x2640, 0xFE0F),
//            Emoji(268, "Detective", 0x1F575, 0xFE0F),
            Emoji(269, "Man Detective", 0x1F575, 0xFE0F, 0x200D, 0x2642, 0xFE0F),
            Emoji(270, "Woman Detective", 0x1F575, 0xFE0F, 0x200D, 0x2640, 0xFE0F),
//            Emoji(271, "Guard", 0x1F482),
            Emoji(272, "Man Guard", 0x1F482, 0x200D, 0x2642, 0xFE0F),
            Emoji(273, "Woman Guard", 0x1F482, 0x200D, 0x2640, 0xFE0F),
//            Emoji(274, "Construction Worker", 0x1F477),
            Emoji(275, "Man Construction Worker", 0x1F477, 0x200D, 0x2642, 0xFE0F),
            Emoji(276, "Woman Construction Worker", 0x1F477, 0x200D, 0x2640, 0xFE0F),
            Emoji(277, "Prince", 0x1F934),
            Emoji(278, "Princess", 0x1F478),
//            Emoji(279, "Person Wearing Turban", 0x1F473),
            Emoji(280, "Man Wearing Turban", 0x1F473, 0x200D, 0x2642, 0xFE0F),
            Emoji(281, "Woman Wearing Turban", 0x1F473, 0x200D, 0x2640, 0xFE0F),
            Emoji(282, "Person With Skullcap", 0x1F472),
            Emoji(283, "Woman with Headscarf", 0x1F9D5),
            Emoji(284, "Person in Tuxedo", 0x1F935),
            Emoji(285, "Person With Veil", 0x1F470),
            Emoji(286, "Pregnant Woman", 0x1F930),
            Emoji(287, "Breast-Feeding", 0x1F931),
//            Emoji(288, "Person Feeding Baby", 0x1F469, 0x200D, 0x1F37C),
            Emoji(289, "Baby Angel", 0x1F47C),
            Emoji(290, "Santa Claus", 0x1F385),
            Emoji(291, "Mrs. Claus", 0x1F936),
            Emoji(292, "Superhero", 0x1F9B8),
//            Emoji(293, "Man Superhero", 0x1F9B8, 0x200D, 0x2642, 0xFE0F),
            Emoji(294, "Woman Superhero", 0x1F9B8, 0x200D, 0x2640, 0xFE0F),
//            Emoji(295, "Supervillain", 0x1F9B9),
            Emoji(296, "Man Supervillain", 0x1F9B9, 0x200D, 0x2642, 0xFE0F),
            Emoji(297, "Woman Supervillain", 0x1F9B9, 0x200D, 0x2640, 0xFE0F),
//            Emoji(298, "Mage", 0x1F9D9),
            Emoji(299, "Man Mage", 0x1F9D9, 0x200D, 0x2642, 0xFE0F),
            Emoji(300, "Woman Mage", 0x1F9D9, 0x200D, 0x2640, 0xFE0F),
//            Emoji(301, "Fairy", 0x1F9DA),
            Emoji(302, "Man Fairy", 0x1F9DA, 0x200D, 0x2642, 0xFE0F),
            Emoji(303, "Woman Fairy", 0x1F9DA, 0x200D, 0x2640, 0xFE0F),
//            Emoji(304, "Vampire", 0x1F9DB),
            Emoji(305, "Man Vampire", 0x1F9DB, 0x200D, 0x2642, 0xFE0F),
            Emoji(306, "Woman Vampire", 0x1F9DB, 0x200D, 0x2640, 0xFE0F),
//            Emoji(307, "Merperson", 0x1F9DC),
            Emoji(308, "Merman", 0x1F9DC, 0x200D, 0x2642, 0xFE0F),
            Emoji(309, "Mermaid", 0x1F9DC, 0x200D, 0x2640, 0xFE0F),
//            Emoji(310, "Elf", 0x1F9DD),
            Emoji(311, "Man Elf", 0x1F9DD, 0x200D, 0x2642, 0xFE0F),
            Emoji(312, "Woman Elf", 0x1F9DD, 0x200D, 0x2640, 0xFE0F),
//            Emoji(313, "Genie", 0x1F9DE),
            Emoji(314, "Man Genie", 0x1F9DE, 0x200D, 0x2642, 0xFE0F),
            Emoji(315, "Woman Genie", 0x1F9DE, 0x200D, 0x2640, 0xFE0F),
//            Emoji(316, "Zombie", 0x1F9DF),
            Emoji(317, "Man Zombie", 0x1F9DF, 0x200D, 0x2642, 0xFE0F),
            Emoji(318, "Woman Zombie", 0x1F9DF, 0x200D, 0x2640, 0xFE0F),
//            Emoji(319, "Person Getting Massage", 0x1F486),
            Emoji(320, "Man Getting Massage", 0x1F486, 0x200D, 0x2642, 0xFE0F),
            Emoji(321, "Woman Getting Massage", 0x1F486, 0x200D, 0x2640, 0xFE0F),
//            Emoji(322, "Person Getting Haircut", 0x1F487),
            Emoji(323, "Man Getting Haircut", 0x1F487, 0x200D, 0x2642, 0xFE0F),
            Emoji(324, "Woman Getting Haircut", 0x1F487, 0x200D, 0x2640, 0xFE0F),
//            Emoji(325, "Person Walking", 0x1F6B6),
            Emoji(326, "Man Walking", 0x1F6B6, 0x200D, 0x2642, 0xFE0F),
            Emoji(327, "Woman Walking", 0x1F6B6, 0x200D, 0x2640, 0xFE0F),
//            Emoji(328, "Person Standing", 0x1F9CD),
//            Emoji(329, "Man Standing", 0x1F9CD, 0x200D, 0x2642, 0xFE0F),
//            Emoji(330, "Woman Standing", 0x1F9CD, 0x200D, 0x2640, 0xFE0F),
//            Emoji(331, "Person Kneeling", 0x1F9CE),
//            Emoji(332, "Man Kneeling", 0x1F9CE, 0x200D, 0x2642, 0xFE0F),
//            Emoji(333, "Woman Kneeling", 0x1F9CE, 0x200D, 0x2640, 0xFE0F),
//            Emoji(334, "Person with Probing Cane", 0x1F9D1, 0x200D, 0x1F9AF),
//            Emoji(335, "Man with Probing Cane", 0x1F468, 0x200D, 0x1F9AF),
//            Emoji(336, "Woman with Probing Cane", 0x1F469, 0x200D, 0x1F9AF),
//            Emoji(337, "Person in Motorized Wheelchair", 0x1F9D1, 0x200D, 0x1F9BC),
//            Emoji(338, "Man in Motorized Wheelchair", 0x1F468, 0x200D, 0x1F9BC),
//            Emoji(339, "Woman in Motorized Wheelchair", 0x1F469, 0x200D, 0x1F9BC),
//            Emoji(340, "Person in Manual Wheelchair", 0x1F9D1, 0x200D, 0x1F9BD),
//            Emoji(341, "Man in Manual Wheelchair", 0x1F468, 0x200D, 0x1F9BD),
//            Emoji(342, "Woman in Manual Wheelchair", 0x1F469, 0x200D, 0x1F9BD),
//            Emoji(343, "Person Running", 0x1F3C3),
            Emoji(344, "Man Running", 0x1F3C3, 0x200D, 0x2642, 0xFE0F),
            Emoji(345, "Woman Running", 0x1F3C3, 0x200D, 0x2640, 0xFE0F),
            Emoji(346, "Woman Dancing", 0x1F483),
            Emoji(347, "Man Dancing", 0x1F57A),
            Emoji(348, "Person in Suit Levitating", 0x1F574, 0xFE0F),
//            Emoji(349, "People with Bunny Ears", 0x1F46F),
            Emoji(350, "Men with Bunny Ears", 0x1F46F, 0x200D, 0x2642, 0xFE0F),
            Emoji(351, "Women with Bunny Ears", 0x1F46F, 0x200D, 0x2640, 0xFE0F),
//            Emoji(352, "Person in Steamy Room", 0x1F9D6, 0x200D, 0x2642, 0xFE0F),
            Emoji(353, "Man in Steamy Room", 0x1F9D6, 0x200D, 0x2642, 0xFE0F),
            Emoji(354, "Woman in Steamy Room", 0x1F9D6, 0x200D, 0x2640, 0xFE0F),
//            Emoji(355, "Person in Lotus Position", 0x1F9D8),
//            Emoji(356, "People Holding Hands", 0x1F9D1, 0x200D, 0x1F91D, 0x200D, 0x1F9D1),
            Emoji(357, "Women Holding Hands", 0x1F46D),
            Emoji(358, "Woman and Man Holding Hands", 0x1F46B),
            Emoji(359, "Men Holding Hands", 0x1F46C),
            Emoji(360, "Kiss", 0x1F48F),
            Emoji(
                361,
                "Kiss: Man, Man",
                0x1F468,
                0x200D,
                0x2764,
                0xFE0F,
                0x200D,
                0x1F48B,
                0x200D,
                0x1F468
            ),
            Emoji(
                362,
                "Kiss: Woman, Woman",
                0x1F469,
                0x200D,
                0x2764,
                0xFE0F,
                0x200D,
                0x1F48B,
                0x200D,
                0x1F469
            ),
            Emoji(363, "Couple with Heart", 0x1F491),
            Emoji(
                364,
                "Couple with Heart: Man, Man",
                0x1F468,
                0x200D,
                0x2764,
                0xFE0F,
                0x200D,
                0x1F468
            ),
            Emoji(
                365,
                "Couple with Heart: Woman, Woman",
                0x1F469,
                0x200D,
                0x2764,
                0xFE0F,
                0x200D,
                0x1F469
            ),
            Emoji(366, "Family", 0x1F46A),
            Emoji(367, "Family: Man, Woman, Boy", 0x1F468, 0x200D, 0x1F469, 0x200D, 0x1F466),
            Emoji(368, "Family: Man, Woman, Girl", 0x1F468, 0x200D, 0x1F469, 0x200D, 0x1F467),
            Emoji(
                369,
                "Family: Man, Woman, Girl, Boy",
                0x1F468,
                0x200D,
                0x1F469,
                0x200D,
                0x1F467,
                0x200D,
                0x1F466
            ),
            Emoji(
                370,
                "Family: Man, Woman, Boy, Boy",
                0x1F468,
                0x200D,
                0x1F469,
                0x200D,
                0x1F466,
                0x200D,
                0x1F466
            ),
            Emoji(
                371,
                "Family: Man, Woman, Girl, Girl",
                0x1F468,
                0x200D,
                0x1F469,
                0x200D,
                0x1F467,
                0x200D,
                0x1F467
            ),
            Emoji(372, "Family: Man, Man, Boy", 0x1F468, 0x200D, 0x1F468, 0x200D, 0x1F466),
            Emoji(373, "Family: Man, Man, Girl", 0x1F468, 0x200D, 0x1F468, 0x200D, 0x1F467),
            Emoji(
                374,
                "Family: Man, Man, Girl, Boy",
                0x1F468,
                0x200D,
                0x1F468,
                0x200D,
                0x1F467,
                0x200D,
                0x1F466
            ),
            Emoji(
                375,
                "Family: Man, Man, Boy, Boy",
                0x1F468,
                0x200D,
                0x1F468,
                0x200D,
                0x1F466,
                0x200D,
                0x1F466
            ),
            Emoji(
                376,
                "Family: Man, Man, Girl, Girl",
                0x1F468,
                0x200D,
                0x1F468,
                0x200D,
                0x1F467,
                0x200D,
                0x1F467
            ),
            Emoji(377, "Family: Woman, Woman, Boy", 0x1F469, 0x200D, 0x1F469, 0x200D, 0x1F466),
            Emoji(378, "Family: Woman, Woman, Girl", 0x1F469, 0x200D, 0x1F469, 0x200D, 0x1F467),
            Emoji(
                379,
                "Family: Woman, Woman, Girl, Boy",
                0x1F469,
                0x200D,
                0x1F469,
                0x200D,
                0x1F467,
                0x200D,
                0x1F466
            ),
            Emoji(
                380,
                "Family: Woman, Woman, Boy, Boy",
                0x1F469,
                0x200D,
                0x1F469,
                0x200D,
                0x1F466,
                0x200D,
                0x1F466
            ),
            Emoji(
                381,
                "Family: Woman, Woman, Girl, Girl",
                0x1F469,
                0x200D,
                0x1F469,
                0x200D,
                0x1F467,
                0x200D,
                0x1F467
            ),
            Emoji(382, "Family: Man, Boy", 0x1F468, 0x200D, 0x1F466),
            Emoji(383, "Family: Man, Boy, Boy", 0x1F468, 0x200D, 0x1F466, 0x200D, 0x1F466),
            Emoji(384, "Family: Man, Girl", 0x1F468, 0x200D, 0x1F467),
            Emoji(385, "Family: Man, Girl, Boy", 0x1F468, 0x200D, 0x1F467, 0x200D, 0x1F466),
            Emoji(386, "Family: Man, Girl, Girl", 0x1F468, 0x200D, 0x1F467, 0x200D, 0x1F467),
            Emoji(387, "Family: Woman, Boy", 0x1F469, 0x200D, 0x1F466),
            Emoji(388, "Family: Woman, Boy, Boy", 0x1F469, 0x200D, 0x1F466, 0x200D, 0x1F466),
            Emoji(389, "Family: Woman, Girl", 0x1F469, 0x200D, 0x1F467),
            Emoji(390, "Family: Woman, Girl, Boy", 0x1F469, 0x200D, 0x1F467, 0x200D, 0x1F466),
            Emoji(391, "Family: Woman, Girl, Girl", 0x1F469, 0x200D, 0x1F467, 0x200D, 0x1F467),
            Emoji(392, "Speaking Head", 0x1F5E3),
            Emoji(393, "Bust in Silhouette", 0x1F464),
            Emoji(394, "Busts in Silhouette", 0x1F465),
            Emoji(395, "Footprints", 0x1F463),
            Emoji(396, "Luggage", 0x1F9F3),
            Emoji(397, "Closed Umbrella", 0x1F302),
            Emoji(398, "Umbrella", 0x2602, 0xFE0F),
            Emoji(399, "Thread", 0x1F9F5),
            Emoji(400, "Yarn", 0x1F9F6),
            Emoji(401, "Glasses", 0x1F453),
            Emoji(402, "Sunglasses", 0x1F576),
            Emoji(403, "Goggles", 0x1F97D),
            Emoji(404, "Lab Coat", 0x1F97C),
//            Emoji(405, "Safety Vest", 0x1F9BA),
            Emoji(406, "Necktie", 0x1F454),
            Emoji(407, "T-Shirt", 0x1F455),
            Emoji(408, "Jeans", 0x1F456),
            Emoji(409, "Scarf", 0x1F9E3),
            Emoji(410, "Gloves", 0x1F9E4),
            Emoji(411, "Coat", 0x1F9E5),
            Emoji(412, "Socks", 0x1F9E6),
            Emoji(413, "Dress", 0x1F457),
            Emoji(414, "Kimono", 0x1F458),
//            Emoji(415, "Sari", 0x1F97B),
//            Emoji(416, "One-Piece Swimsuit", 0x1FA71),
//            Emoji(417, "Briefs", 0x1FA72),
//            Emoji(418, "Shorts", 0x1FA73),
            Emoji(419, "Bikini", 0x1F459),
            Emoji(420, "Woman???s Clothes", 0x1F45A),
            Emoji(421, "Purse", 0x1F45B),
            Emoji(422, "Handbag", 0x1F45C),
            Emoji(423, "Clutch Bag", 0x1F45D),
            Emoji(424, "Backpack", 0x1F392),
            Emoji(425, "Man???s Shoe", 0x1F45E),
            Emoji(426, "Running Shoe", 0x1F45F),
            Emoji(427, "Hiking Boot", 0x1F97E),
            Emoji(428, "Flat Shoe", 0x1F97F),
            Emoji(429, "High-Heeled Shoe", 0x1F460),
            Emoji(430, "Woman???s Sandal", 0x1F461),
//            Emoji(431, "Ballet Shoes", 0x1FA70),
            Emoji(432, "Woman???s Boot", 0x1F462),
            Emoji(433, "Crown", 0x1F451),
            Emoji(434, "Woman???s Hat", 0x1F452),
            Emoji(435, "Top Hat", 0x1F3A9),
            Emoji(436, "Graduation Cap", 0x1F393),
            Emoji(437, "Billed Cap", 0x1F9E2),
            Emoji(438, "Rescue Worker???s Helmet", 0x26D1, 0xFE0F),
            Emoji(439, "Lipstick", 0x1F484),
            Emoji(440, "Ring", 0x1F48D),
            Emoji(441, "Briefcase", 0x1F4BC)
        )
    }
}