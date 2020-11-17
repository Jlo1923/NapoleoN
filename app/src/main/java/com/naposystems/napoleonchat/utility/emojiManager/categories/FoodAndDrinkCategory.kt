package com.naposystems.napoleonchat.utility.emojiManager.categories

import com.naposystems.napoleonchat.model.emojiKeyboard.Emoji
import com.naposystems.napoleonchat.model.emojiKeyboard.EmojiCategory
import com.naposystems.napoleonchat.utility.Constants
import java.io.Serializable

class FoodAndDrinkCategory : EmojiCategory(), Serializable {

    init {
        this.id = Constants.EmojiCategory.FOOD_AND_DRINK.category
        this.name = "Food & Drink"
        this.emojiList = arrayListOf(
            Emoji(1, "Grapes", 0x1F347),
            Emoji(2, "Melon", 0x1F348),
            Emoji(3, "Watermelon", 0x1F349),
            Emoji(4, "Tangerine", 0x1F34A),
            Emoji(5, "Lemon", 0x1F34B),
            Emoji(6, "Banana", 0x1F34C),
            Emoji(7, "Pineapple", 0x1F34D),
            Emoji(8, "Mango", 0x1F96D),
            Emoji(9, "Red Apple", 0x1F34E),
            Emoji(10, "Green Apple", 0x1F34F),
            Emoji(11, "Pear", 0x1F350),
            Emoji(12, "Peach", 0x1F351),
            Emoji(13, "Cherries", 0x1F352),
            Emoji(14, "Strawberry", 0x1F353),
            Emoji(15, "Kiwi Fruit", 0x1F95D),
            Emoji(16, "Tomato", 0x1F345),
            Emoji(17, "Coconut", 0x1F965),
            Emoji(18, "Avocado", 0x1F951),
            Emoji(19, "Eggplant", 0x1F346),
            Emoji(20, "Potato", 0x1F954),
            Emoji(21, "Carrot", 0x1F955),
            Emoji(22, "Ear of Corn", 0x1F33D),
            Emoji(23, "Hot Pepper", 0x1F336),
            Emoji(24, "Cucumber", 0x1F952),
            Emoji(25, "Leafy Green", 0x1F96C),
            Emoji(26, "Broccoli", 0x1F966),
//            Emoji(27, "Garlic", 0x1F9C4),
//            Emoji(28, "Onion", 0x1F9C5),
            Emoji(29, "Mushroom", 0x1F344),
            Emoji(30, "Peanuts", 0x1F95C),
            Emoji(31, "Chestnut", 0x1F330),
            Emoji(32, "Bread", 0x1F35E),
            Emoji(33, "Croissant", 0x1F950),
            Emoji(34, "Baguette Bread", 0x1F956),
            Emoji(35, "Pretzel", 0x1F968),
            Emoji(36, "Bagel", 0x1F96F),
            Emoji(37, "Pancakes", 0x1F95E),
//            Emoji(38, "Waffle", 0x1F9C7),
            Emoji(39, "Cheese Wedge", 0x1F9C0),
            Emoji(40, "Meat on Bone", 0x1F356),
            Emoji(41, "Poultry Leg", 0x1F357),
            Emoji(42, "Cut of Meat", 0x1F969),
            Emoji(43, "Bacon", 0x1F953),
            Emoji(44, "Hamburger", 0x1F354),
            Emoji(45, "French Fries", 0x1F35F),
            Emoji(46, "Pizza", 0x1F355),
            Emoji(47, "Hot Dog", 0x1F32D),
            Emoji(48, "Sandwich", 0x1F96A),
            Emoji(49, "Taco", 0x1F32E),
            Emoji(50, "Burrito", 0x1F32F),
            Emoji(51, "Stuffed Flatbread", 0x1F959),
//            Emoji(52, "Falafel", 0x1F9C6),
            Emoji(53, "Cooking", 0x1F373),
            Emoji(54, "Shallow Pan of Food", 0x1F958),
            Emoji(55, "Pot of Food", 0x1F372),
            Emoji(56, "Bowl with Spoon", 0x1F963),
            Emoji(57, "Green Salad", 0x1F957),
            Emoji(58, "Popcorn", 0x1F37F),
//            Emoji(59, "Butter", 0x1F9C8),
            Emoji(60, "Salt", 0x1F9C2),
            Emoji(61, "Canned Food", 0x1F96B),
            Emoji(62, "Bento Box", 0x1F371),
            Emoji(63, "Rice Cracker", 0x1F358),
            Emoji(64, "Rice Ball", 0x1F359),
            Emoji(65, "Cooked Rice", 0x1F35A),
            Emoji(66, "Curry Rice", 0x1F35B),
            Emoji(67, "Steaming Bowl", 0x1F35C),
            Emoji(68, "Spaghetti", 0x1F35D),
            Emoji(69, "Roasted Sweet Potato", 0x1F360),
            Emoji(70, "Oden", 0x1F362),
            Emoji(71, "Sushi", 0x1F363),
            Emoji(72, "Fried Shrimp", 0x1F364),
            Emoji(73, "Fish Cake with Swirl", 0x1F365),
            Emoji(74, "Moon Cake", 0x1F96E),
            Emoji(75, "Dango", 0x1F361),
            Emoji(76, "Dumpling", 0x1F95F),
            Emoji(77, "Fortune Cookie", 0x1F960),
            Emoji(78, "Takeout Box", 0x1F961),
//            Emoji(79, "Oyster", 0x1F9AA),
            Emoji(80, "Soft Ice Cream", 0x1F366),
            Emoji(81, "Shaved Ice", 0x1F367),
            Emoji(82, "Ice Cream", 0x1F368),
            Emoji(83, "Doughnut", 0x1F369),
            Emoji(84, "Cookie", 0x1F36A),
            Emoji(85, "Birthday Cake", 0x1F382),
            Emoji(86, "Shortcake", 0x1F370),
            Emoji(87, "Cupcake", 0x1F9C1),
            Emoji(88, "Pie", 0x1F967),
            Emoji(89, "Chocolate Bar", 0x1F36B),
            Emoji(90, "Candy", 0x1F36C),
            Emoji(91, "Lollipop", 0x1F36D),
            Emoji(92, "Custard", 0x1F36E),
            Emoji(93, "Honey Pot", 0x1F36F),
            Emoji(94, "Baby Bottle", 0x1F37C),
            Emoji(95, "Glass of Milk", 0x1F95B),
            Emoji(96, "Hot Beverage", 0x2615),
            Emoji(97, "Teacup Without Handle", 0x1F375),
            Emoji(98, "Sake", 0x1F376),
            Emoji(99, "Bottle with Popping Cork", 0x1F37E),
            Emoji(100, "Wine Glass", 0x1F377),
            Emoji(101, "Cocktail Glass", 0x1F378),
            Emoji(102, "Tropical Drink", 0x1F379),
            Emoji(103, "Beer Mug", 0x1F37A),
            Emoji(104, "Clinking Beer Mugs", 0x1F37B),
            Emoji(105, "Clinking Glasses", 0x1F942),
            Emoji(106, "Tumbler Glass", 0x1F943),
            Emoji(107, "Cup with Straw", 0x1F964),
//            Emoji(108, "Beverage Box", 0x1F9C3),
//            Emoji(109, "Mate", 0x1F9C9),
//            Emoji(110, "Ice", 0x1F9CA),
            Emoji(111, "Chopsticks", 0x1F962),
            Emoji(112, "Fork and Knife with Plate", 0x1F37D),
            Emoji(113, "Fork and Knife", 0x1F374),
            Emoji(114, "Spoon", 0x1F944)
        )
    }
}