package com.naposystems.pepito.utility.emojiManager.categories

import com.naposystems.pepito.model.emojiKeyboard.Emoji
import com.naposystems.pepito.model.emojiKeyboard.EmojiCategory
import com.naposystems.pepito.utility.Constants

class ObjectsCategory : EmojiCategory() {

    init {
        this.id = Constants.EmojiCategory.OBJECTS.category
        this.name = "Objects"
        this.emojiList = arrayListOf(
            Emoji(1, "Love Letter", 0x1F48C),
            Emoji(2, "Hole", 0x1F573),
            Emoji(3, "Bomb", 0x1F4A3),
            Emoji(4, "Person Taking Bath", 0x1F6C0),
            Emoji(5, "Person in Bed", 0x1F6CC),
            Emoji(6, "Kitchen Knife", 0x1F52A),
            Emoji(7, "Amphora", 0x1F3FA),
            Emoji(8, "World Map", 0x1F5FA),
            Emoji(9, "Compass", 0x1F9ED),
            Emoji(10, "Brick", 0x1F9F1),
            Emoji(11, "Barber Pole", 0x1F488),
//            Emoji(12, "Manual Wheelchair", 0x1F9BD),
//            Emoji(13, "Motorized Wheelchair", 0x1F9BC),
            Emoji(14, "Oil Drum", 0x1F6E2),
            Emoji(15, "Bellhop Bell", 0x1F6CE),
            Emoji(16, "Luggage", 0x1F9F3),
            Emoji(17, "Hourglass Done", 0x231B),
            Emoji(18, "Hourglass Not Done", 0x23F3),
            Emoji(19, "Watch", 0x231A),
            Emoji(20, "Alarm Clock", 0x23F0),
            Emoji(21, "Stopwatch", 0x23F1, 0xFE0F),
            Emoji(22, "Timer Clock", 0x23F2, 0xFE0F),
            Emoji(23, "Mantelpiece Clock", 0x1F570, 0xFE0F),
            Emoji(24, "Thermometer", 0x1F321),
//            Emoji(25, "Umbrella on Ground", 0x26F1),
            Emoji(26, "Firecracker", 0x1F9E8),
            Emoji(27, "Balloon", 0x1F388),
            Emoji(28, "Party Popper", 0x1F389),
            Emoji(29, "Confetti Ball", 0x1F38A),
            Emoji(30, "Japanese Dolls", 0x1F38E),
            Emoji(31, "Carp Streamer", 0x1F38F),
            Emoji(32, "Wind Chime", 0x1F390),
            Emoji(33, "Red Envelope", 0x1F9E7),
            Emoji(34, "Ribbon", 0x1F380),
            Emoji(35, "Wrapped Gift", 0x1F381),
//            Emoji(36, "Diving Mask", 0x1F93F),
//            Emoji(37, "Yo-Yo", 0x1FA80),
//            Emoji(38, "Kite", 0x1FA81),
            Emoji(39, "Crystal Ball", 0x1F52E),
            Emoji(40, "Nazar Amulet", 0x1F9FF),
            Emoji(41, "Joystick", 0x1F579),
            Emoji(42, "Teddy Bear", 0x1F9F8),
            Emoji(43, "Framed Picture", 0x1F5BC),
            Emoji(44, "Thread", 0x1F9F5),
            Emoji(45, "Yarn", 0x1F9F6),
            Emoji(46, "Shopping Bags", 0x1F6CD),
            Emoji(47, "Prayer Beads", 0x1F4FF),
            Emoji(48, "Gem Stone", 0x1F48E),
            Emoji(49, "Postal Horn", 0x1F4EF),
            Emoji(50, "Studio Microphone", 0x1F399),
            Emoji(51, "Level Slider", 0x1F39A),
            Emoji(52, "Control Knobs", 0x1F39B),
            Emoji(53, "Radio", 0x1F4FB),
//            Emoji(54, "Banjo", 0x1FA95),
            Emoji(55, "Mobile Phone", 0x1F4F1),
            Emoji(56, "Mobile Phone with Arrow", 0x1F4F2),
            Emoji(57, "Telephone", 0x260E),
            Emoji(58, "Telephone Receiver", 0x1F4DE),
            Emoji(59, "Pager", 0x1F4DF),
            Emoji(60, "Fax Machine", 0x1F4E0),
            Emoji(61, "Battery", 0x1F50B),
            Emoji(62, "Electric Plug", 0x1F50C),
            Emoji(63, "Laptop", 0x1F4BB),
            Emoji(64, "Desktop Computer", 0x1F5A5),
            Emoji(65, "Printer", 0x1F5A8),
            Emoji(66, "Keyboard", 0x2328, 0xFE0F),
            Emoji(67, "Computer Mouse", 0x1F5B1),
            Emoji(68, "Trackball", 0x1F5B2),
            Emoji(69, "Computer Disk", 0x1F4BD),
            Emoji(70, "Floppy Disk", 0x1F4BE),
            Emoji(71, "Optical Disk", 0x1F4BF),
            Emoji(72, "DVD", 0x1F4C0),
            Emoji(73, "Abacus", 0x1F9EE),
            Emoji(74, "Movie Camera", 0x1F3A5),
            Emoji(75, "Film Frames", 0x1F39E),
            Emoji(76, "Film Projector", 0x1F4FD),
            Emoji(77, "Television", 0x1F4FA),
            Emoji(78, "Camera", 0x1F4F7),
            Emoji(79, "Camera with Flash", 0x1F4F8),
            Emoji(80, "Video Camera", 0x1F4F9),
            Emoji(81, "Videocassette", 0x1F4FC),
            Emoji(82, "Magnifying Glass Tilted Left", 0x1F50D),
            Emoji(83, "Magnifying Glass Tilted Right", 0x1F50E),
            Emoji(84, "Candle", 0x1F56F),
            Emoji(85, "Light Bulb", 0x1F4A1),
            Emoji(86, "Flashlight", 0x1F526),
            Emoji(87, "Red Paper Lantern", 0x1F3EE),
//            Emoji(88, "Diya Lamp", 0x1FA94),
            Emoji(89, "Notebook with Decorative Cover", 0x1F4D4),
            Emoji(90, "Closed Book", 0x1F4D5),
            Emoji(91, "Open Book", 0x1F4D6),
            Emoji(92, "Green Book", 0x1F4D7),
            Emoji(93, "Blue Book", 0x1F4D8),
            Emoji(94, "Orange Book", 0x1F4D9),
            Emoji(95, "Books", 0x1F4DA),
            Emoji(96, "Notebook", 0x1F4D3),
            Emoji(97, "Page with Curl", 0x1F4C3),
            Emoji(98, "Scroll", 0x1F4DC),
            Emoji(99, "Page Facing Up", 0x1F4C4),
            Emoji(100, "Newspaper", 0x1F4F0),
            Emoji(101, "Rolled-Up Newspaper", 0x1F5DE),
            Emoji(102, "Bookmark Tabs", 0x1F4D1),
            Emoji(103, "Bookmark", 0x1F516),
            Emoji(104, "Label", 0x1F3F7),
            Emoji(105, "Money Bag", 0x1F4B0),
            Emoji(106, "Yen Banknote", 0x1F4B4),
            Emoji(107, "Dollar Banknote", 0x1F4B5),
            Emoji(108, "Euro Banknote", 0x1F4B6),
            Emoji(109, "Pound Banknote", 0x1F4B7),
            Emoji(110, "Money with Wings", 0x1F4B8),
            Emoji(111, "Credit Card", 0x1F4B3),
            Emoji(112, "Receipt", 0x1F9FE),
            Emoji(113, "Envelope", 0x2709),
            Emoji(114, "E-Mail", 0x1F4E7),
            Emoji(115, "Incoming Envelope", 0x1F4E8),
            Emoji(116, "Envelope with Arrow", 0x1F4E9),
            Emoji(117, "Outbox Tray", 0x1F4E4),
            Emoji(118, "Inbox Tray", 0x1F4E5),
            Emoji(119, "Package", 0x1F4E6),
            Emoji(120, "Closed Mailbox with Raised Flag", 0x1F4EB),
            Emoji(121, "Closed Mailbox with Lowered Flag", 0x1F4EA),
            Emoji(122, "Open Mailbox with Raised Flag", 0x1F4EC),
            Emoji(123, "Open Mailbox with Lowered Flag", 0x1F4ED),
            Emoji(124, "Postbox", 0x1F4EE),
            Emoji(125, "Ballot Box with Ballot", 0x1F5F3),
            Emoji(126, "Pencil", 0x270F, 0xFE0F),
            Emoji(127, "Black Nib", 0x2712, 0xFE0F),
            Emoji(128, "Fountain Pen", 0x1F58B, 0xFE0F),
            Emoji(129, "Pen", 0x1F58A, 0xFE0F),
            Emoji(130, "Paintbrush", 0x1F58C, 0xFE0F),
            Emoji(131, "Crayon", 0x1F58D, 0xFE0F),
            Emoji(132, "Memo", 0x1F4DD),
            Emoji(133, "File Folder", 0x1F4C1),
            Emoji(134, "Open File Folder", 0x1F4C2),
            Emoji(135, "Card Index Dividers", 0x1F5C2),
            Emoji(136, "Calendar", 0x1F4C5),
            Emoji(137, "Tear-Off Calendar", 0x1F4C6),
            Emoji(138, "Spiral Notepad", 0x1F5D2),
            Emoji(139, "Spiral Calendar", 0x1F5D3),
            Emoji(140, "Card Index", 0x1F4C7),
            Emoji(141, "Chart Increasing", 0x1F4C8),
            Emoji(142, "Chart Decreasing", 0x1F4C9),
            Emoji(143, "Bar Chart", 0x1F4CA),
            Emoji(144, "Clipboard", 0x1F4CB),
            Emoji(145, "Pushpin", 0x1F4CC),
            Emoji(146, "Round Pushpin", 0x1F4CD),
            Emoji(147, "Paperclip", 0x1F4CE),
            Emoji(148, "Linked Paperclips", 0x1F587),
            Emoji(149, "Straight Ruler", 0x1F4CF),
            Emoji(150, "Triangular Ruler", 0x1F4D0),
            Emoji(151, "Scissors", 0x2702, 0xFE0F),
            Emoji(152, "Card File Box", 0x1F5C3),
            Emoji(153, "File Cabinet", 0x1F5C4),
            Emoji(154, "Wastebasket", 0x1F5D1),
            Emoji(155, "Locked", 0x1F512),
            Emoji(156, "Unlocked", 0x1F513),
            Emoji(157, "Locked with Pen", 0x1F50F),
            Emoji(158, "Locked with Key", 0x1F510),
            Emoji(159, "Key", 0x1F511),
            Emoji(160, "Old Key", 0x1F5DD),
            Emoji(161, "Hammer", 0x1F528),
//            Emoji(162, "Axe", 0x1FA93),
            Emoji(163, "Pick", 0x26CF, 0xFE0F),
            Emoji(164, "Hammer and Pick", 0x2692, 0xFE0F),
            Emoji(165, "Hammer and Wrench", 0x1F6E0, 0xFE0F),
            Emoji(166, "Dagger", 0x1F5E1, 0xFE0F),
            Emoji(167, "Crossed Swords", 0x2694, 0xFE0F),
            Emoji(168, "Pistol", 0x1F52B),
            Emoji(169, "Shield", 0x1F6E1),
            Emoji(170, "Wrench", 0x1F527),
            Emoji(171, "Nut and Bolt", 0x1F529),
            Emoji(172, "Gear", 0x2699, 0xFE0F),
            Emoji(173, "Clamp", 0x1F5DC),
            Emoji(174, "Balance Scale", 0x2696, 0xFE0F),
//            Emoji(175, "Probing Cane", 0x1F9AF),
            Emoji(176, "Link", 0x1F517),
            Emoji(177, "Chains", 0x26D3, 0xFE0F),
            Emoji(178, "Toolbox", 0x1F9F0),
            Emoji(179, "Magnet", 0x1F9F2),
            Emoji(180, "Alembic", 0x2697, 0xFE0F),
            Emoji(181, "Test Tube", 0x1F9EA),
            Emoji(182, "Petri Dish", 0x1F9EB),
            Emoji(183, "DNA", 0x1F9EC),
            Emoji(184, "Microscope", 0x1F52C),
            Emoji(185, "Telescope", 0x1F52D),
            Emoji(186, "Satellite Antenna", 0x1F4E1),
            Emoji(187, "Syringe", 0x1F489),
//            Emoji(188, "Drop of Blood", 0x1FA78),
            Emoji(189, "Pill", 0x1F48A),
//            Emoji(190, "Adhesive Bandage", 0x1FA79),
//            Emoji(191, "Stethoscope", 0x1FA7A),
            Emoji(192, "Door", 0x1F6AA),
            Emoji(193, "Bed", 0x1F6CF),
            Emoji(194, "Couch and Lamp", 0x1F6CB),
//            Emoji(195, "Chair", 0x1FA91),
            Emoji(196, "Toilet", 0x1F6BD),
            Emoji(197, "Shower", 0x1F6BF),
            Emoji(198, "Bathtub", 0x1F6C1),
//            Emoji(199, "Razor", 0x1FA92),
            Emoji(200, "Lotion Bottle", 0x1F9F4),
            Emoji(201, "Safety Pin", 0x1F9F7),
            Emoji(202, "Broom", 0x1F9F9),
            Emoji(203, "Basket", 0x1F9FA),
            Emoji(204, "Roll of Paper", 0x1F9FB),
            Emoji(205, "Soap", 0x1F9FC),
            Emoji(206, "Sponge", 0x1F9FD),
            Emoji(207, "Fire Extinguisher", 0x1F9EF),
            Emoji(208, "Cigarette", 0x1F6AC),
            Emoji(209, "Coffin", 0x26B0, 0xFE0F),
            Emoji(210, "Funeral Urn", 0x26B1, 0xFE0F),
            Emoji(211, "Moai", 0x1F5FF),
            Emoji(212, "Potable Water", 0x1F6B0)
        )
    }
}