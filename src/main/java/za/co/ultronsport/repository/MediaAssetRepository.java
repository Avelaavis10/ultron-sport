package za.co.ultronsport.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.ultronsport.domain.MediaAsset;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    List<MediaAsset> findByOwnerUserId(Long ownerUserId);
}
