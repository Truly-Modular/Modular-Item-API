package smartin.miapi.material.generated;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import smartin.miapi.Miapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * this class handles finding a sensible name for Generated Materials
 */
public class NamingUtil {
    public static String generateTranslation(List<TieredItem> items, ItemStack mainIngredient) {
        List<String> names = new ArrayList<>();
        items.forEach(item -> names.add(Component.translatable(item.getDescriptionId()).getString()));
        String materialName = Component.translatable(mainIngredient.getDescriptionId()).getString();
        String fakeTranslation = findCommonSubstring(names, materialName);
        return fakeTranslation.trim();
    }


    static String findCommonSubstring(List<String> itemNames, String materialName) {
        Map<String, Integer> map = new HashMap<>();
        int highestScore = 0;
        String bestSubstring = "";

        // Compare materialName to all itemNames
        for (String itemName : itemNames) {
            String commonString = longestSubsString(itemName, materialName);
            if (commonString.length() > 3) {
                int score = calculateScore(commonString, 4); // 4x weight for materialName comparison
                map.put(commonString, map.getOrDefault(commonString, 0) + score);
            }
        }

        // Compare each itemName to each other
        for (int i = 0; i < itemNames.size(); i++) {
            for (int j = i + 1; j < itemNames.size(); j++) {
                String commonString = longestSubsString(itemNames.get(i), itemNames.get(j));
                if (commonString.length() > 3) {
                    int score = calculateScore(commonString, 1); // Normal weight for other comparisons
                    map.put(commonString, map.getOrDefault(commonString, 0) + score);
                }
            }
        }

        // Find the substring with the highest score
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                bestSubstring = entry.getKey();
            }
        }

        return bestSubstring;
    }

    // Helper method to calculate the score for a substring
    private static int calculateScore(String substring, int weight) {
        int length = substring.length();
        int occurrences = 1; // Each occurrence is counted with a base score of 3
        return weight * (length + (occurrences * 3));
    }


    static String findCommonSubstringOld(List<String> itemNames, String materialName) {
        Map<String, Integer> map = new HashMap<>();
        map.put(materialName, 1);
        int highest = 0;
        String longestCommonSubstring = materialName;
        for (String itemName : itemNames) {
            String commonString = longestSubsString(itemName, materialName);
            if (commonString.length() > 3) {
                if (map.containsKey(commonString)) {
                    map.put(commonString, map.get(commonString) + 1);
                    if (map.get(commonString) > highest) {
                        highest = map.get(commonString);
                        longestCommonSubstring = commonString;
                    }
                } else {
                    map.put(commonString, 1);
                }
            }
        }
        return longestCommonSubstring;
    }

    static String longestSubsString(String stringA, String stringB) {
        // Find length of both the Strings.
        if (stringB == null || stringA == null) {
            return "";
        }
        try {
            if (stringB.length() > stringA.length()) {
                String buffer = stringA;
                stringA = stringB;
                stringB = buffer;
            }
            int aLength = stringA.length();
            int bLength = stringB.length();

            // Variable to store length of longest common subString.
            int result = 0;

            // Variable to store ending point of longest common subString in stringA.
            int end = 0;

            // Matrix to store result of two consecutive rows at a time.
            int[][] len = new int[2][bLength + 1]; // Adjusted initialization

            // Variable to represent which row of matrix is current row.
            int currRow = 0;

            // For a particular value of i and j, len[currRow][j] stores length of longest
            // common subString in String X[0..i] and Y[0..j].
            for (int i = 0; i <= aLength; i++) {
                for (int j = 0; j <= bLength; j++) {
                    if (i == 0 || j == 0) {
                        len[currRow][j] = 0;
                    } else if (stringA.charAt(i - 1) == stringB.charAt(j - 1)) {
                        len[currRow][j] = len[1 - currRow][j - 1] + 1;
                        if (len[currRow][j] > result) {
                            result = len[currRow][j];
                            end = i;
                        }
                    } else {
                        len[currRow][j] = 0;
                    }
                }

                // Make current row as previous row and previous row as new current row.
                currRow = 1 - currRow;
            }
            if (result == 0) {
                return "";
            }

            // Longest common subString is from index end - result to index end in stringA.
            return stringA.substring(end - result, end);
        } catch (Exception e) {
            Miapi.LOGGER.warn("Exception during string comparison", e);
            return "";
        }
    }


}
