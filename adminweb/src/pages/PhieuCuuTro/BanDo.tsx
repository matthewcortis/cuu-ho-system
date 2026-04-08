import { useEffect, useRef, useState } from "react";
import PageBreadcrumb from "../../components/common/PageBreadCrumb";
import ComponentCard from "../../components/common/ComponentCard";
import PageMeta from "../../components/common/PageMeta";
import {
  TRACK_ASIA_CSS_URL,
  TRACK_ASIA_DEFAULT_CENTER,
  TRACK_ASIA_DEFAULT_ZOOM,
  TRACK_ASIA_SCRIPT_URL,
  TRACK_ASIA_STYLE_URL,
} from "../../api/trackAsia";

type LocationStatus = "idle" | "requesting" | "granted" | "denied" | "error";

type TrackAsiaCenter = {
  lat: number;
  lng: number;
};

interface TrackAsiaMapInstance {
  setCenter: (center: TrackAsiaCenter) => void;
  setZoom: (zoom: number) => void;
  flyTo?: (options: { center: TrackAsiaCenter; zoom?: number }) => void;
  remove: () => void;
}

interface TrackAsiaMarkerInstance {
  setLngLat: (lngLat: [number, number]) => TrackAsiaMarkerInstance;
  addTo: (map: TrackAsiaMapInstance) => TrackAsiaMarkerInstance;
  remove: () => void;
}

interface TrackAsiaGlobal {
  Map: new (options: {
    container: string | HTMLElement;
    style: string;
    center: TrackAsiaCenter;
    zoom: number;
  }) => TrackAsiaMapInstance;
  Marker?: new (options?: { color?: string }) => TrackAsiaMarkerInstance;
}

declare global {
  interface Window {
    trackasiagl?: TrackAsiaGlobal;
  }
}

function ensureTrackAsiaCss(): void {
  const cssId = "trackasia-gl-css";
  if (document.getElementById(cssId)) return;

  const linkElement = document.createElement("link");
  linkElement.id = cssId;
  linkElement.rel = "stylesheet";
  linkElement.href = TRACK_ASIA_CSS_URL;
  document.head.appendChild(linkElement);
}

function loadTrackAsiaSdk(): Promise<TrackAsiaGlobal> {
  if (window.trackasiagl) {
    return Promise.resolve(window.trackasiagl);
  }

  ensureTrackAsiaCss();

  return new Promise((resolve, reject) => {
    const scriptId = "trackasia-gl-script";
    const existingScript = document.getElementById(scriptId) as HTMLScriptElement | null;

    const handleLoad = () => {
      if (window.trackasiagl) {
        resolve(window.trackasiagl);
      } else {
        reject(new Error("Khong tim thay trackasiagl sau khi tai SDK."));
      }
    };

    const handleError = () => {
      reject(new Error("Khong the tai TrackAsia SDK."));
    };

    if (existingScript) {
      existingScript.addEventListener("load", handleLoad, { once: true });
      existingScript.addEventListener("error", handleError, { once: true });
      return;
    }

    const scriptElement = document.createElement("script");
    scriptElement.id = scriptId;
    scriptElement.src = TRACK_ASIA_SCRIPT_URL;
    scriptElement.async = true;
    scriptElement.addEventListener("load", handleLoad, { once: true });
    scriptElement.addEventListener("error", handleError, { once: true });
    document.body.appendChild(scriptElement);
  });
}

function getLocationErrorMessage(code: number): string {
  if (code === 1) return "Ban da tu choi cap quyen vi tri.";
  if (code === 2) return "Khong the xac dinh vi tri hien tai.";
  if (code === 3) return "Het thoi gian lay vi tri. Vui long thu lai.";
  return "Khong lay duoc vi tri. Vui long thu lai.";
}

export default function     BanDoPage() {
  const mapContainerRef = useRef<HTMLDivElement | null>(null);
  const mapRef = useRef<TrackAsiaMapInstance | null>(null);
  const markerRef = useRef<TrackAsiaMarkerInstance | null>(null);

  const [mapReady, setMapReady] = useState(false);
  const [mapError, setMapError] = useState("");
  const [locationStatus, setLocationStatus] = useState<LocationStatus>("idle");
  const [locationMessage, setLocationMessage] = useState("");

  useEffect(() => {
    let disposed = false;

    loadTrackAsiaSdk()
      .then((trackasia) => {
        if (disposed || !mapContainerRef.current) return;

        mapRef.current = new trackasia.Map({
          container: mapContainerRef.current,
          style: TRACK_ASIA_STYLE_URL,
          center: TRACK_ASIA_DEFAULT_CENTER,
          zoom: TRACK_ASIA_DEFAULT_ZOOM,
        });
        setMapReady(true);
      })
      .catch((error) => {
        setMapError(error instanceof Error ? error.message : "Khong the tai ban do.");
      });

    return () => {
      disposed = true;
      markerRef.current?.remove();
      markerRef.current = null;
      mapRef.current?.remove();
      mapRef.current = null;
    };
  }, []);

  const handleLocateMe = () => {
    if (!mapRef.current) {
      setLocationStatus("error");
      setLocationMessage("Ban do chua san sang.");
      return;
    }

    if (!navigator.geolocation) {
      setLocationStatus("error");
      setLocationMessage("Trinh duyet khong ho tro Geolocation.");
      return;
    }

    setLocationStatus("requesting");
    setLocationMessage("Dang yeu cau quyen vi tri...");

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const center = {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        };

        const map = mapRef.current;
        if (map) {
          if (map.flyTo) {
            map.flyTo({ center, zoom: 13 });
          } else {
            map.setCenter(center);
            map.setZoom(13);
          }
        }

        const trackasia = window.trackasiagl;
        if (trackasia?.Marker && map) {
          markerRef.current?.remove();
          markerRef.current = new trackasia.Marker({ color: "#465fff" })
            .setLngLat([center.lng, center.lat])
            .addTo(map);
        }

        setLocationStatus("granted");
        setLocationMessage(`Vi tri cua ban: ${center.lat.toFixed(6)}, ${center.lng.toFixed(6)}`);
      },
      (error) => {
        setLocationStatus(error.code === 1 ? "denied" : "error");
        setLocationMessage(getLocationErrorMessage(error.code));
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      }
    );
  };

  return (
    <>
      <PageMeta
        title="Ban do - Phieu cuu tro"
        description="Ban do TrackAsia va tinh nang lay vi tri nguoi dung."
      />
      <PageBreadcrumb pageTitle="Ban do" />

      <div className="space-y-6">
        <ComponentCard
          title="Ban do cuu tro"
          desc="Map duoc khoi tao tu TrackAsia, co the lay vi tri hien tai cua ban."
        >
          <div className="space-y-4">
            <div className="flex flex-wrap items-center gap-3">
              <button
                type="button"
                onClick={handleLocateMe}
                disabled={!mapReady || locationStatus === "requesting"}
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:bg-brand-300"
              >
                {locationStatus === "requesting" ? "Dang lay vi tri..." : "Lay vi tri cua toi"}
              </button>

              {mapError && (
                <span className="text-theme-xs text-error-600 dark:text-error-400">
                  {mapError}
                </span>
              )}

              {locationMessage && (
                <span className="text-theme-xs text-gray-600 dark:text-gray-300" aria-live="polite">
                  {locationMessage}
                </span>
              )}
            </div>

            <div
              id="map"
              ref={mapContainerRef}
              className="h-[520px] w-full overflow-hidden rounded-xl border border-gray-200 dark:border-gray-700"
            />
          </div>
        </ComponentCard>
      </div>
    </>
  );
}
