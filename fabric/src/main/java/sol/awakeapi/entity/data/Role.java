package sol.awakeapi.entity.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.item.Items;

import java.util.stream.Collectors;

public class Role {

    public static String getRoles(Entity entity) {
        if (entity instanceof MerchantEntity merchant) {
            return merchant.getOffers().stream()
                    .map(offer -> {
                        String sellItem = offer.getSellItem() + "(s)";
                        String firstBuyItem = offer.getOriginalFirstBuyItem() + "(s)";
                        String secondBuyItem = "";
                        if (!offer.getSecondBuyItem().isEmpty() && !offer.getSecondBuyItem().getItem().equals(Items.AIR)) {
                            secondBuyItem = " and " + offer.getSecondBuyItem() + "(s)";
                        }
                        return "[" + sellItem + "] for [" + firstBuyItem + secondBuyItem + "]";
                    })
                    .collect(Collectors.joining("\n"));
        }
        return "";
    }
}
