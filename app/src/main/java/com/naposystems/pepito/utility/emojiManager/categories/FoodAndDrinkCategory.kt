package com.naposystems.pepito.utility.emojiManager.categories

import com.naposystems.pepito.model.emojiKeyboard.Emoji
import com.naposystems.pepito.model.emojiKeyboard.EmojiCategory

class FoodAndDrinkCategory : EmojiCategory() {

    init {
        this.id = 3
        this.name = "Food & Drink"
        this.emojiList = arrayListOf(
            Emoji(id = 1, name = "Grapes", code = 0x1F347),
            Emoji(id = 2, name = "Melon", code = 0x1F348),
            Emoji(id = 3, name = "Watermelon", code = 0x1F349),
            Emoji(id = 4, name = "Tangerine", code = 0x1F34A),
            Emoji(id = 5, name = "Lemon", code = 0x1F34B),
            Emoji(id = 6, name = "Banana", code = 0x1F34C),
            Emoji(id = 7, name = "Pineapple", code = 0x1F34D),
            Emoji(id = 8, name = "Mango", code = 0x1F96D),
            Emoji(id = 9, name = "Red Apple", code = 0x1F34E),
            Emoji(id = 10, name = "Green Apple", code = 0x1F34F),
            Emoji(id = 11, name = "Pear", code = 0x1F350),
            Emoji(id = 12, name = "Peach", code = 0x1F351),
            Emoji(id = 13, name = "Cherries", code = 0x1F352),
            Emoji(id = 14, name = "Strawberry", code = 0x1F353),
            Emoji(id = 15, name = "Kiwi Fruit", code = 0x1F95D),
            Emoji(id = 16, name = "Tomato", code = 0x1F345),
            Emoji(id = 17, name = "Coconut", code = 0x1F965),
            Emoji(id = 18, name = "Avocado", code = 0x1F951),
            Emoji(id = 19, name = "Eggplant", code = 0x1F346),
            Emoji(id = 20, name = "Potato", code = 0x1F954),
            Emoji(id = 21, name = "Carrot", code = 0x1F955),
            Emoji(id = 22, name = "Ear of Corn", code = 0x1F33D),
            Emoji(id = 23, name = "Hot Pepper", code = 0x1F336),
            Emoji(id = 24, name = "Cucumber", code = 0x1F952),
            Emoji(id = 25, name = "Leafy Green", code = 0x1F96C),
            Emoji(id = 26, name = "Broccoli", code = 0x1F966),
            Emoji(id = 27, name = "Garlic", code = 0x1F9C4),
            Emoji(id = 28, name = "Onion", code = 0x1F9C5),
            Emoji(id = 29, name = "Mushroom", code = 0x1F344),
            Emoji(id = 30, name = "Peanuts", code = 0x1F95C),
            Emoji(id = 31, name = "Chestnut", code = 0x1F330),
            Emoji(id = 32, name = "Bread", code = 0x1F35E),
            Emoji(id = 33, name = "Croissant", code = 0x1F950),
            Emoji(id = 34, name = "Baguette Bread", code = 0x1F956),
            Emoji(id = 35, name = "Pretzel", code = 0x1F968),
            Emoji(id = 36, name = "Bagel", code = 0x1F96F),
            Emoji(id = 37, name = "Pancakes", code = 0x1F95E),
            Emoji(id = 38, name = "Waffle", code = 0x1F9C7),
            Emoji(id = 39, name = "Cheese Wedge", code = 0x1F9C0),
            Emoji(id = 40, name = "Meat on Bone", code = 0x1F356),
            Emoji(id = 41, name = "Poultry Leg", code = 0x1F357),
            Emoji(id = 42, name = "Cut of Meat", code = 0x1F969),
            Emoji(id = 43, name = "Bacon", code = 0x1F953),
            Emoji(id = 44, name = "Hamburger", code = 0x1F354),
            Emoji(id = 45, name = "French Fries", code = 0x1F35F),
            Emoji(id = 46, name = "Pizza", code = 0x1F355),
            Emoji(id = 47, name = "Hot Dog", code = 0x1F32D),
            Emoji(id = 48, name = "Sandwich", code = 0x1F96A),
            Emoji(id = 49, name = "Taco", code = 0x1F32E),
            Emoji(id = 50, name = "Burrito", code = 0x1F32F),
            Emoji(id = 51, name = "Stuffed Flatbread", code = 0x1F959),
            Emoji(id = 52, name = "Falafel", code = 0x1F9C6),
            Emoji(id = 53, name = "Cooking", code = 0x1F373),
            Emoji(id = 54, name = "Shallow Pan of Food", code = 0x1F958),
            Emoji(id = 55, name = "Pot of Food", code = 0x1F372),
            Emoji(id = 56, name = "Bowl with Spoon", code = 0x1F963),
            Emoji(id = 57, name = "Green Salad", code = 0x1F957),
            Emoji(id = 58, name = "Popcorn", code = 0x1F37F),
            Emoji(id = 59, name = "Butter", code = 0x1F9C8),
            Emoji(id = 60, name = "Salt", code = 0x1F9C2),
            Emoji(id = 61, name = "Canned Food", code = 0x1F96B),
            Emoji(id = 62, name = "Bento Box", code = 0x1F371),
            Emoji(id = 63, name = "Rice Cracker", code = 0x1F358),
            Emoji(id = 64, name = "Rice Ball", code = 0x1F359),
            Emoji(id = 65, name = "Cooked Rice", code = 0x1F35A),
            Emoji(id = 66, name = "Curry Rice", code = 0x1F35B),
            Emoji(id = 67, name = "Steaming Bowl", code = 0x1F35C),
            Emoji(id = 68, name = "Spaghetti", code = 0x1F35D),
            Emoji(id = 69, name = "Roasted Sweet Potato", code = 0x1F360),
            Emoji(id = 70, name = "Oden", code = 0x1F362),
            Emoji(id = 71, name = "Sushi", code = 0x1F363),
            Emoji(id = 72, name = "Fried Shrimp", code = 0x1F364),
            Emoji(id = 73, name = "Fish Cake with Swirl", code = 0x1F365),
            Emoji(id = 74, name = "Moon Cake", code = 0x1F96E),
            Emoji(id = 75, name = "Dango", code = 0x1F361),
            Emoji(id = 76, name = "Dumpling", code = 0x1F95F),
            Emoji(id = 77, name = "Fortune Cookie", code = 0x1F960),
            Emoji(id = 78, name = "Takeout Box", code = 0x1F961),
            Emoji(id = 79, name = "Oyster", code = 0x1F9AA),
            Emoji(id = 80, name = "Soft Ice Cream", code = 0x1F366),
            Emoji(id = 81, name = "Shaved Ice", code = 0x1F367),
            Emoji(id = 82, name = "Ice Cream", code = 0x1F368),
            Emoji(id = 83, name = "Doughnut", code = 0x1F369),
            Emoji(id = 84, name = "Cookie", code = 0x1F36A),
            Emoji(id = 85, name = "Birthday Cake", code = 0x1F382),
            Emoji(id = 86, name = "Shortcake", code = 0x1F370),
            Emoji(id = 87, name = "Cupcake", code = 0x1F9C1),
            Emoji(id = 88, name = "Pie", code = 0x1F967),
            Emoji(id = 89, name = "Chocolate Bar", code = 0x1F36B),
            Emoji(id = 90, name = "Candy", code = 0x1F36C),
            Emoji(id = 91, name = "Lollipop", code = 0x1F36D),
            Emoji(id = 92, name = "Custard", code = 0x1F36E),
            Emoji(id = 93, name = "Honey Pot", code = 0x1F36F),
            Emoji(id = 94, name = "Baby Bottle", code = 0x1F37C),
            Emoji(id = 95, name = "Glass of Milk", code = 0x1F95B),
            Emoji(id = 96, name = "Hot Beverage", code = 0x2615),
            Emoji(id = 97, name = "Teacup Without Handle", code = 0x1F375),
            Emoji(id = 98, name = "Sake", code = 0x1F376),
            Emoji(id = 99, name = "Bottle with Popping Cork", code = 0x1F37E),
            Emoji(id = 100, name = "Wine Glass", code = 0x1F377),
            Emoji(id = 101, name = "Cocktail Glass", code = 0x1F378),
            Emoji(id = 102, name = "Tropical Drink", code = 0x1F379),
            Emoji(id = 103, name = "Beer Mug", code = 0x1F37A),
            Emoji(id = 104, name = "Clinking Beer Mugs", code = 0x1F37B),
            Emoji(id = 105, name = "Clinking Glasses", code = 0x1F942),
            Emoji(id = 106, name = "Tumbler Glass", code = 0x1F943),
            Emoji(id = 107, name = "Cup with Straw", code = 0x1F964),
            Emoji(id = 108, name = "Beverage Box", code = 0x1F9C3),
            Emoji(id = 109, name = "Mate", code = 0x1F9C9),
            Emoji(id = 110, name = "Ice", code = 0x1F9CA),
            Emoji(id = 111, name = "Chopsticks", code = 0x1F962),
            Emoji(id = 112, name = "Fork and Knife with Plate", code = 0x1F37D),
            Emoji(id = 113, name = "Fork and Knife", code = 0x1F374),
            Emoji(id = 114, name = "Spoon", code = 0x1F944)
        )
    }
}