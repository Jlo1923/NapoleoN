package com.naposystems.pepito.utility.emojiManager.categories

import com.naposystems.pepito.model.emojiKeyboard.Emoji
import com.naposystems.pepito.model.emojiKeyboard.EmojiCategory
import com.naposystems.pepito.utility.Constants
import java.io.Serializable

class FlagsCategory : EmojiCategory(), Serializable {

    init {
        this.id = Constants.EmojiCategory.FLAGS.category
        this.name = "Flags"
        this.emojiList = arrayListOf(
            Emoji(1, "Chequered Flag", 0x1F3C1),
            Emoji(2, "Triangular Flag", 0x1F6A9),
            Emoji(3, "Crossed Flags", 0x1F38C),
            Emoji(4, "Black Flag", 0x1F3F4),
            Emoji(5, "White Flag", 0x1F3F3, 0xFE0F),
            Emoji(6, "Rainbow Flag", 0x1F3F3, 0xFE0F, 0x200D, 0x1F308),
            Emoji(8, "Pirate Flag", 0x1F3F4, 0x200D, 0x2620, 0xFE0F),
            Emoji(9, "Flag: Ascension Island", 0x1F1E6, 0x1F1E8),
            Emoji(10, "Flag: Andorra", 0x1F1E6, 0x1F1E9),
            Emoji(11, "Flag: United Arab Emirates", 0x1F1E6, 0x1F1EA),
            Emoji(12, "Flag: Afghanistan", 0x1F1E6, 0x1F1EB),
            Emoji(13, "Flag: Antigua & Barbuda", 0x1F1E6, 0x1F1EC),
            Emoji(14, "Flag: Anguilla", 0x1F1E6, 0x1F1EE),
            Emoji(15, "Flag: Albania", 0x1F1E6, 0x1F1F1),
            Emoji(16, "Flag: Armenia", 0x1F1E6, 0x1F1F2),
            Emoji(17, "Flag: Angola", 0x1F1E6, 0x1F1F4),
            Emoji(18, "Flag: Antarctica", 0x1F1E6, 0x1F1F6),
            Emoji(19, "Flag: Argentina", 0x1F1E6, 0x1F1F7),
            Emoji(20, "Flag: American Samoa", 0x1F1E6, 0x1F1F8),
            Emoji(21, "Flag: Austria", 0x1F1E6, 0x1F1F9),
            Emoji(22, "Flag: Australia", 0x1F1E6, 0x1F1FA),
            Emoji(23, "Flag: Aruba", 0x1F1E6, 0x1F1FC),
            Emoji(24, "Flag: Åland Islands", 0x1F1E6, 0x1F1FD),
            Emoji(25, "Flag: Azerbaijan", 0x1F1E6, 0x1F1FF),
            Emoji(26, "Flag: Bosnia & Herzegovina", 0x1F1E7, 0x1F1E6),
            Emoji(27, "Flag: Barbados", 0x1F1E7, 0x1F1E7),
            Emoji(28, "Flag: Bangladesh", 0x1F1E7, 0x1F1E9),
            Emoji(29, "Flag: Belgium", 0x1F1E7, 0x1F1EA),
            Emoji(30, "Flag: Burkina Faso", 0x1F1E7, 0x1F1EB),
            Emoji(31, "Flag: Bulgaria", 0x1F1E7, 0x1F1EC),
            Emoji(32, "Flag: Bahrain", 0x1F1E7, 0x1F1ED),
            Emoji(33, "Flag: Burundi", 0x1F1E7, 0x1F1EE),
            Emoji(34, "Flag: Benin", 0x1F1E7, 0x1F1EF),
//            Emoji(35, "Flag: St. Barthélemy", 0x1F1E7, 0x1F1F1),
            Emoji(36, "Flag: Bermuda", 0x1F1E7, 0x1F1F2),
            Emoji(37, "Flag: Brunei", 0x1F1E7, 0x1F1F3),
            Emoji(38, "Flag: Bolivia", 0x1F1E7, 0x1F1F4),
//            Emoji(39, "Flag: Caribbean Netherlands", 0x1F1E7, 0x1F1F6),
            Emoji(40, "Flag: Brazil", 0x1F1E7, 0x1F1F7),
            Emoji(41, "Flag: Bahamas", 0x1F1E7, 0x1F1F8),
            Emoji(42, "Flag: Bhutan", 0x1F1E7, 0x1F1F9),
            Emoji(43, "Flag: Bouvet Island", 0x1F1E7, 0x1F1FB),
            Emoji(44, "Flag: Botswana", 0x1F1E7, 0x1F1FC),
            Emoji(45, "Flag: Belarus", 0x1F1E7, 0x1F1FE),
            Emoji(46, "Flag: Belize", 0x1F1E7, 0x1F1FF),
            Emoji(47, "Flag: Canada", 0x1F1E8, 0x1F1E6),
            Emoji(48, "Flag: Cocos (Keeling), Islands", 0x1F1E8, 0x1F1E8),
            Emoji(49, "Flag: Congo - Kinshasa", 0x1F1E8, 0x1F1E9),
            Emoji(50, "Flag: Central African Republic", 0x1F1E8, 0x1F1EB),
            Emoji(51, "Flag: Congo - Brazzaville", 0x1F1E8, 0x1F1EC),
            Emoji(52, "Flag: Switzerland", 0x1F1E8, 0x1F1ED),
            Emoji(53, "Flag: Côte d’Ivoire", 0x1F1E8, 0x1F1EE),
            Emoji(54, "Flag: Cook Islands", 0x1F1E8, 0x1F1F0),
            Emoji(55, "Flag: Chile", 0x1F1E8, 0x1F1F1),
            Emoji(56, "Flag: Cameroon", 0x1F1E8, 0x1F1F2),
            Emoji(57, "Flag: China", 0x1F1E8, 0x1F1F3),
            Emoji(58, "Flag: Colombia", 0x1F1E8, 0x1F1F4),
            Emoji(59, "Flag: Clipperton Island", 0x1F1E8, 0x1F1F5),
            Emoji(60, "Flag: Costa Rica", 0x1F1E8, 0x1F1F7),
            Emoji(61, "Flag: Cuba", 0x1F1E8, 0x1F1FA),
            Emoji(62, "Flag: Cape Verde", 0x1F1E8, 0x1F1FB),
            Emoji(63, "Flag: Curaçao", 0x1F1E8, 0x1F1FC),
            Emoji(64, "Flag: Christmas Island", 0x1F1E8, 0x1F1FD),
            Emoji(65, "Flag: Cyprus", 0x1F1E8, 0x1F1FE),
            Emoji(66, "Flag: Czechia", 0x1F1E8, 0x1F1FF),
            Emoji(67, "Flag: Germany", 0x1F1E9, 0x1F1EA),
            Emoji(68, "Flag: Diego Garcia", 0x1F1E9, 0x1F1EC),
            Emoji(69, "Flag: Djibouti", 0x1F1E9, 0x1F1EF),
            Emoji(70, "Flag: Denmark", 0x1F1E9, 0x1F1F0),
            Emoji(71, "Flag: Dominica", 0x1F1E9, 0x1F1F2),
            Emoji(72, "Flag: Dominican Republic", 0x1F1E9, 0x1F1F4),
            Emoji(73, "Flag: Algeria", 0x1F1E9, 0x1F1FF),
            Emoji(74, "Flag: Ceuta & Melilla", 0x1F1EA, 0x1F1E6),
            Emoji(75, "Flag: Ecuador", 0x1F1EA, 0x1F1E8),
            Emoji(76, "Flag: Estonia", 0x1F1EA, 0x1F1EA),
            Emoji(77, "Flag: Egypt", 0x1F1EA, 0x1F1EC),
            Emoji(78, "Flag: Western Sahara", 0x1F1EA, 0x1F1ED),
            Emoji(79, "Flag: Eritrea", 0x1F1EA, 0x1F1F7),
            Emoji(80, "Flag: Spain", 0x1F1EA, 0x1F1F8),
            Emoji(81, "Flag: Ethiopia", 0x1F1EA, 0x1F1F9),
            Emoji(82, "Flag: European Union", 0x1F1EA, 0x1F1FA),
            Emoji(83, "Flag: Finland", 0x1F1EB, 0x1F1EE),
            Emoji(84, "Flag: Fiji", 0x1F1EB, 0x1F1EF),
            Emoji(85, "Flag: Falkland Islands", 0x1F1EB, 0x1F1F0),
            Emoji(86, "Flag: Micronesia", 0x1F1EB, 0x1F1F2),
            Emoji(87, "Flag: Faroe Islands", 0x1F1EB, 0x1F1F4),
            Emoji(88, "Flag: France", 0x1F1EB, 0x1F1F7),
            Emoji(89, "Flag: Gabon", 0x1F1EC, 0x1F1E6),
            Emoji(90, "Flag: United Kingdom", 0x1F1EC, 0x1F1E7),
            Emoji(91, "Flag: Grenada", 0x1F1EC, 0x1F1E9),
            Emoji(92, "Flag: Georgia", 0x1F1EC, 0x1F1EA),
            Emoji(93, "Flag: French Guiana", 0x1F1EC, 0x1F1EB),
            Emoji(94, "Flag: Guernsey", 0x1F1EC, 0x1F1EC),
            Emoji(95, "Flag: Ghana", 0x1F1EC, 0x1F1ED),
            Emoji(96, "Flag: Gibraltar", 0x1F1EC, 0x1F1EE),
            Emoji(97, "Flag: Greenland", 0x1F1EC, 0x1F1F1),
            Emoji(98, "Flag: Gambia", 0x1F1EC, 0x1F1F2),
            Emoji(99, "Flag: Guinea", 0x1F1EC, 0x1F1F3),
            Emoji(100, "Flag: Guadeloupe", 0x1F1EC, 0x1F1F5),
            Emoji(101, "Flag: Equatorial Guinea", 0x1F1EC, 0x1F1F6),
            Emoji(102, "Flag: Greece", 0x1F1EC, 0x1F1F7),
            Emoji(103, "Flag: South Georgia & South Sandwich Islands", 0x1F1EC, 0x1F1F8),
            Emoji(104, "Flag: Guatemala", 0x1F1EC, 0x1F1F9),
            Emoji(105, "Flag: Guam", 0x1F1EC, 0x1F1FA),
            Emoji(106, "Flag: Guinea-Bissau", 0x1F1EC, 0x1F1FC),
            Emoji(107, "Flag: Guyana", 0x1F1EC, 0x1F1FE),
            Emoji(108, "Flag: Hong Kong SAR China", 0x1F1ED, 0x1F1F0),
            Emoji(109, "Flag: Heard & McDonald Islands", 0x1F1ED, 0x1F1F2),
            Emoji(110, "Flag: Honduras", 0x1F1ED, 0x1F1F3),
            Emoji(111, "Flag: Croatia", 0x1F1ED, 0x1F1F7),
            Emoji(112, "Flag: Haiti", 0x1F1ED, 0x1F1F9),
            Emoji(113, "Flag: Hungary", 0x1F1ED, 0x1F1FA),
            Emoji(114, "Flag: Canary Islands", 0x1F1EE, 0x1F1E8),
            Emoji(115, "Flag: Indonesia", 0x1F1EE, 0x1F1E9),
            Emoji(116, "Flag: Ireland", 0x1F1EE, 0x1F1EA),
            Emoji(117, "Flag: Israel", 0x1F1EE, 0x1F1F1),
            Emoji(118, "Flag: Isle of Man", 0x1F1EE, 0x1F1F2),
            Emoji(119, "Flag: India", 0x1F1EE, 0x1F1F3),
            Emoji(120, "Flag: British Indian Ocean Territory", 0x1F1EE, 0x1F1F4),
            Emoji(121, "Flag: Iraq", 0x1F1EE, 0x1F1F6),
            Emoji(122, "Flag: Iran", 0x1F1EE, 0x1F1F7),
            Emoji(123, "Flag: Iceland", 0x1F1EE, 0x1F1F8),
            Emoji(124, "Flag: Italy", 0x1F1EE, 0x1F1F9),
            Emoji(125, "Flag: Jersey", 0x1F1EF, 0x1F1EA),
            Emoji(126, "Flag: Jamaica", 0x1F1EF, 0x1F1F2),
            Emoji(127, "Flag: Jordan", 0x1F1EF, 0x1F1F4),
            Emoji(128, "Flag: Japan", 0x1F1EF, 0x1F1F5),
            Emoji(129, "Flag: Kenya", 0x1F1F0, 0x1F1EA),
            Emoji(130, "Flag: Kyrgyzstan", 0x1F1F0, 0x1F1EC),
            Emoji(131, "Flag: Cambodia", 0x1F1F0, 0x1F1ED),
            Emoji(132, "Flag: Kiribati", 0x1F1F0, 0x1F1EE),
            Emoji(133, "Flag: Comoros", 0x1F1F0, 0x1F1F2),
            Emoji(134, "Flag: St. Kitts & Nevis", 0x1F1F0, 0x1F1F3),
            Emoji(135, "Flag: North Korea", 0x1F1F0, 0x1F1F5),
            Emoji(136, "Flag: South Korea", 0x1F1F0, 0x1F1F7),
            Emoji(137, "Flag: Kuwait", 0x1F1F0, 0x1F1FC),
            Emoji(138, "Flag: Cayman Islands", 0x1F1F0, 0x1F1FE),
            Emoji(139, "Flag: Kazakhstan", 0x1F1F0, 0x1F1FF),
            Emoji(140, "Flag: Laos", 0x1F1F1, 0x1F1E6),
            Emoji(141, "Flag: Lebanon", 0x1F1F1, 0x1F1E7),
            Emoji(142, "Flag: St. Lucia", 0x1F1F1, 0x1F1E8),
            Emoji(143, "Flag: Liechtenstein", 0x1F1F1, 0x1F1EE),
            Emoji(144, "Flag: Sri Lanka", 0x1F1F1, 0x1F1F0),
            Emoji(145, "Flag: Liberia", 0x1F1F1, 0x1F1F7),
            Emoji(146, "Flag: Lesotho", 0x1F1F1, 0x1F1F8),
            Emoji(147, "Flag: Lithuania", 0x1F1F1, 0x1F1F9),
            Emoji(148, "Flag: Luxembourg", 0x1F1F1, 0x1F1FA),
            Emoji(149, "Flag: Latvia", 0x1F1F1, 0x1F1FB),
            Emoji(150, "Flag: Libya", 0x1F1F1, 0x1F1FE),
            Emoji(151, "Flag: Morocco", 0x1F1F2, 0x1F1E6),
            Emoji(152, "Flag: Monaco", 0x1F1F2, 0x1F1E8),
            Emoji(153, "Flag: Moldova", 0x1F1F2, 0x1F1E9),
            Emoji(154, "Flag: Montenegro", 0x1F1F2, 0x1F1EA),
            Emoji(155, "Flag: St. Martin", 0x1F1F2, 0x1F1EB),
            Emoji(156, "Flag: Madagascar", 0x1F1F2, 0x1F1EC),
            Emoji(157, "Flag: Marshall Islands", 0x1F1F2, 0x1F1ED),
            Emoji(158, "Flag: North Macedonia", 0x1F1F2, 0x1F1F0),
            Emoji(159, "Flag: Mali", 0x1F1F2, 0x1F1F1),
            Emoji(160, "Flag: Myanmar (Burma),", 0x1F1F2, 0x1F1F2),
            Emoji(161, "Flag: Mongolia", 0x1F1F2, 0x1F1F3),
            Emoji(162, "Flag: Macao Sar China", 0x1F1F2, 0x1F1F4),
            Emoji(163, "Flag: Northern Mariana Islands", 0x1F1F2, 0x1F1F5),
//            Emoji(164, "Flag: Martinique", 0x1F1F2, 0x1F1F6),
            Emoji(165, "Flag: Mauritania", 0x1F1F2, 0x1F1F7),
            Emoji(166, "Flag: Montserrat", 0x1F1F2, 0x1F1F8),
            Emoji(167, "Flag: Malta", 0x1F1F2, 0x1F1F9),
            Emoji(168, "Flag: Mauritius", 0x1F1F2, 0x1F1FA),
            Emoji(169, "Flag: Maldives", 0x1F1F2, 0x1F1FB),
            Emoji(170, "Flag: Malawi", 0x1F1F2, 0x1F1FC),
            Emoji(171, "Flag: Mexico", 0x1F1F2, 0x1F1FD),
            Emoji(172, "Flag: Malaysia", 0x1F1F2, 0x1F1FE),
            Emoji(173, "Flag: Mozambique", 0x1F1F2, 0x1F1FF),
            Emoji(174, "Flag: Namibia", 0x1F1F3, 0x1F1E6),
            Emoji(175, "Flag: New Caledonia", 0x1F1F3, 0x1F1E8),
            Emoji(176, "Flag: Niger", 0x1F1F3, 0x1F1EA),
            Emoji(177, "Flag: Norfolk Island", 0x1F1F3, 0x1F1EB),
            Emoji(178, "Flag: Nigeria", 0x1F1F3, 0x1F1EC),
            Emoji(179, "Flag: Nicaragua", 0x1F1F3, 0x1F1EE),
            Emoji(180, "Flag: Netherlands", 0x1F1F3, 0x1F1F1),
            Emoji(181, "Flag: Norway", 0x1F1F3, 0x1F1F4),
            Emoji(182, "Flag: Nepal", 0x1F1F3, 0x1F1F5),
            Emoji(183, "Flag: Nauru", 0x1F1F3, 0x1F1F7),
            Emoji(184, "Flag: Niue", 0x1F1F3, 0x1F1FA),
            Emoji(185, "Flag: New Zealand", 0x1F1F3, 0x1F1FF),
            Emoji(186, "Flag: Oman", 0x1F1F4, 0x1F1F2),
            Emoji(187, "Flag: Panama", 0x1F1F5, 0x1F1E6),
            Emoji(188, "Flag: Peru", 0x1F1F5, 0x1F1EA),
            Emoji(189, "Flag: French Polynesia", 0x1F1F5, 0x1F1EB),
            Emoji(190, "Flag: Papua New Guinea", 0x1F1F5, 0x1F1EC),
            Emoji(191, "Flag: Philippines", 0x1F1F5, 0x1F1ED),
            Emoji(192, "Flag: Pakistan", 0x1F1F5, 0x1F1F0),
            Emoji(193, "Flag: Poland", 0x1F1F5, 0x1F1F1),
            Emoji(194, "Flag: St. Pierre & Miquelon", 0x1F1F5, 0x1F1F2),
            Emoji(195, "Flag: Pitcairn Islands", 0x1F1F5, 0x1F1F3),
            Emoji(196, "Flag: Puerto Rico", 0x1F1F5, 0x1F1F7),
            Emoji(197, "Flag: Palestinian Territories", 0x1F1F5, 0x1F1F8),
            Emoji(198, "Flag: Portugal", 0x1F1F5, 0x1F1F9),
            Emoji(199, "Flag: Palau", 0x1F1F5, 0x1F1FC),
            Emoji(200, "Flag: Paraguay", 0x1F1F5, 0x1F1FE),
            Emoji(201, "Flag: Qatar", 0x1F1F6, 0x1F1E6),
//            Emoji(202, "Flag: Réunion", 0x1F1F7, 0x1F1EA),
            Emoji(203, "Flag: Romania", 0x1F1F7, 0x1F1F4),
            Emoji(204, "Flag: Serbia", 0x1F1F7, 0x1F1F8),
            Emoji(205, "Flag: Russia", 0x1F1F7, 0x1F1FA),
            Emoji(206, "Flag: Rwanda", 0x1F1F7, 0x1F1FC),
            Emoji(207, "Flag: Saudi Arabia", 0x1F1F8, 0x1F1E6),
            Emoji(208, "Flag: Solomon Islands", 0x1F1F8, 0x1F1E7),
            Emoji(209, "Flag: Seychelles", 0x1F1F8, 0x1F1E8),
            Emoji(210, "Flag: Sudan", 0x1F1F8, 0x1F1E9),
            Emoji(211, "Flag: Sweden", 0x1F1F8, 0x1F1EA),
            Emoji(212, "Flag: Singapore", 0x1F1F8, 0x1F1EC),
            Emoji(213, "Flag: St. Helena", 0x1F1F8, 0x1F1ED),
            Emoji(214, "Flag: Slovenia", 0x1F1F8, 0x1F1EE),
            Emoji(215, "Flag: Svalbard & Jan Mayen", 0x1F1F8, 0x1F1EF),
            Emoji(216, "Flag: Slovakia", 0x1F1F8, 0x1F1F0),
            Emoji(217, "Flag: Sierra Leone", 0x1F1F8, 0x1F1F1),
            Emoji(218, "Flag: San Marino", 0x1F1F8, 0x1F1F2),
            Emoji(219, "Flag: Senegal", 0x1F1F8, 0x1F1F3),
            Emoji(220, "Flag: Somalia", 0x1F1F8, 0x1F1F4),
            Emoji(221, "Flag: Suriname", 0x1F1F8, 0x1F1F7),
            Emoji(222, "Flag: South Sudan", 0x1F1F8, 0x1F1F8),
            Emoji(223, "Flag: São Tomé & Príncipe", 0x1F1F8, 0x1F1F9),
            Emoji(224, "Flag: El Salvador", 0x1F1F8, 0x1F1FB),
            Emoji(225, "Flag: Sint Maarten", 0x1F1F8, 0x1F1FD),
            Emoji(226, "Flag: Syria", 0x1F1F8, 0x1F1FE),
            Emoji(227, "Flag: Eswatini", 0x1F1F8, 0x1F1FF),
            Emoji(228, "Flag: Tristan Da Cunha", 0x1F1F9, 0x1F1E6),
            Emoji(229, "Flag: Turks & Caicos Islands", 0x1F1F9, 0x1F1E8),
            Emoji(230, "Flag: Chad", 0x1F1F9, 0x1F1E9),
//            Emoji(231, "Flag: French Southern Territories", 0x1F1F9, 0x1F1EB),
            Emoji(232, "Flag: Togo", 0x1F1F9, 0x1F1EC),
            Emoji(233, "Flag: Thailand", 0x1F1F9, 0x1F1ED),
            Emoji(234, "Flag: Tajikistan", 0x1F1F9, 0x1F1EF),
            Emoji(235, "Flag: Tokelau", 0x1F1F9, 0x1F1F0),
            Emoji(236, "Flag: Timor-Leste", 0x1F1F9, 0x1F1F1),
            Emoji(237, "Flag: Turkmenistan", 0x1F1F9, 0x1F1F2),
            Emoji(238, "Flag: Tunisia", 0x1F1F9, 0x1F1F3),
            Emoji(239, "Flag: Tonga", 0x1F1F9, 0x1F1F4),
            Emoji(240, "Flag: Turkey", 0x1F1F9, 0x1F1F7),
            Emoji(241, "Flag: Trinidad & Tobago", 0x1F1F9, 0x1F1F9),
            Emoji(242, "Flag: Tuvalu", 0x1F1F9, 0x1F1FB),
            Emoji(243, "Flag: Taiwan", 0x1F1F9, 0x1F1FC),
            Emoji(244, "Flag: Tanzania", 0x1F1F9, 0x1F1FF),
            Emoji(245, "Flag: Ukraine", 0x1F1FA, 0x1F1E6),
            Emoji(246, "Flag: Uganda", 0x1F1FA, 0x1F1EC),
            Emoji(247, "Flag: U.S. Outlying Islands", 0x1F1FA, 0x1F1F2),
            Emoji(248, "Flag: United Nations", 0x1F1FA, 0x1F1F3),
            Emoji(249, "Flag: United States", 0x1F1FA, 0x1F1F8),
            Emoji(250, "Flag: Uruguay", 0x1F1FA, 0x1F1FE),
            Emoji(251, "Flag: Uzbekistan", 0x1F1FA, 0x1F1FF),
            Emoji(252, "Flag: Vatican City", 0x1F1FB, 0x1F1E6),
            Emoji(253, "Flag: St. Vincent & Grenadines", 0x1F1FB, 0x1F1E8),
            Emoji(254, "Flag: Venezuela", 0x1F1FB, 0x1F1EA),
            Emoji(255, "Flag: British Virgin Islands", 0x1F1FB, 0x1F1EC),
            Emoji(256, "Flag: U.S. Virgin Islands", 0x1F1FB, 0x1F1EE),
            Emoji(257, "Flag: Vietnam", 0x1F1FB, 0x1F1F3),
            Emoji(258, "Flag: Vanuatu", 0x1F1FB, 0x1F1FA),
            Emoji(259, "Flag: Wallis & Futuna", 0x1F1FC, 0x1F1EB),
            Emoji(260, "Flag: Samoa", 0x1F1FC, 0x1F1F8),
            Emoji(261, "Flag: Kosovo", 0x1F1FD, 0x1F1F0),
            Emoji(262, "Flag: Yemen", 0x1F1FE, 0x1F1EA),
            Emoji(263, "Flag: Mayotte", 0x1F1FE, 0x1F1F9),
            Emoji(264, "Flag: South Africa", 0x1F1FF, 0x1F1E6),
            Emoji(265, "Flag: Zambia", 0x1F1FF, 0x1F1F2),
            Emoji(266, "Flag: Zimbabwe", 0x1F1FF, 0x1F1FC),
            Emoji(
                267,
                "Flag: England",
                0x1F3F4,
                0xE0067,
                0xE0062,
                0xE0065,
                0xE006E,
                0xE0067,
                0xE007F
            ),
            Emoji(
                268,
                "Flag: Scotland",
                0x1F3F4,
                0xE0067,
                0xE0062,
                0xE0073,
                0xE0063,
                0xE0074,
                0xE007F
            ),
            Emoji(269, "Flag: Wales", 0x1F3F4, 0xE0067, 0xE0062, 0xE0077, 0xE006C, 0xE0073, 0xE007F)
        )
    }
}