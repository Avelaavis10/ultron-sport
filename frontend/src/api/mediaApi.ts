import { apiRequest } from "./apiClient";
import type { MediaAssetResponse, UploadMediaResponse } from "../types/apiTypes";

export const mediaApi = {
  upload: (athleteProfileId: number, file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return apiRequest<UploadMediaResponse>("/api/media/upload", {
      method: "POST",
      query: { athleteProfileId },
      body: formData
    });
  },
  getMetadata: (mediaId: number) => apiRequest<MediaAssetResponse>(`/api/media/${mediaId}`)
};
