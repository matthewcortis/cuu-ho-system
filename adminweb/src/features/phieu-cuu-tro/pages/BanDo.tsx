import { useEffect, useRef, useState } from "react";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import ComponentCard from "@/components/common/ComponentCard";
import PageMeta from "@/components/common/PageMeta";
import {
  TRACK_ASIA_CSS_URL,
  TRACK_ASIA_DEFAULT_CENTER,
  TRACK_ASIA_DEFAULT_ZOOM,
  TRACK_ASIA_SCRIPT_URL,
  TRACK_ASIA_STYLE_URL,
} from "@/api/trackAsia";
import {
  fetchPhieuCuuTroList,
  NguoiDungApiError,
  type PhieuCuuTroDto,
} from "@/features/nguoi-dung/api/nguoiDungApi";

type LocationStatus = "idle" | "requesting" | "granted" | "denied" | "error";
type RescueMapStatus = "DANG_TREN_DUONG_TOI" | "DANG_XU_LY" | "HOAN_THANH";

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

type TrackAsiaMarkerOptions = {
  color?: string;
  element?: HTMLElement;
};

interface TrackAsiaGlobal {
  Map: new (options: {
    container: string | HTMLElement;
    style: string;
    center: TrackAsiaCenter;
    zoom: number;
  }) => TrackAsiaMapInstance;
  Marker?: new (options?: TrackAsiaMarkerOptions) => TrackAsiaMarkerInstance;
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

function ensureRescueNodeStyles(): void {
  const styleId = "trackasia-rescue-node-style";
  if (document.getElementById(styleId)) return;

  const styleElement = document.createElement("style");
  styleElement.id = styleId;
  styleElement.textContent = `
    @keyframes rescueNodePulse {
      0% { transform: translate(-50%, -50%) scale(0.5); opacity: 0.6; }
      70% { transform: translate(-50%, -50%) scale(1.35); opacity: 0; }
      100% { transform: translate(-50%, -50%) scale(1.35); opacity: 0; }
    }

    .rescue-map-node {
      --node-color: #ef4444;
      position: relative;
      width: 22px;
      height: 22px;
      pointer-events: none;
    }

    .rescue-map-node--red { --node-color: #ef4444; }
    .rescue-map-node--yellow { --node-color: #f59e0b; }
    .rescue-map-node--green { --node-color: #22c55e; }

    .rescue-map-node__pulse {
      position: absolute;
      top: 50%;
      left: 50%;
      width: 22px;
      height: 22px;
      border-radius: 999px;
      background: var(--node-color);
      opacity: 0;
      animation: rescueNodePulse 1.8s ease-out infinite;
    }

    .rescue-map-node__pulse--2 {
      animation-delay: 0.9s;
    }

    .rescue-map-node__core {
      position: absolute;
      top: 50%;
      left: 50%;
      width: 10px;
      height: 10px;
      border-radius: 999px;
      transform: translate(-50%, -50%);
      background: var(--node-color);
      box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.92);
    }
  `;
  document.head.appendChild(styleElement);
}

function normalizeTrangThaiPhieu(value: string | null | undefined): string {
  if (typeof value !== "string") {
    return "";
  }
  return value.trim().toUpperCase();
}

function parseCoordinate(value: string | null | undefined): number | null {
  if (typeof value !== "string") {
    return null;
  }
  const normalized = value.trim();
  if (!normalized) {
    return null;
  }
  const parsed = Number.parseFloat(
    normalized.includes(",") && !normalized.includes(".")
      ? normalized.replace(",", ".")
      : normalized
  );
  return Number.isFinite(parsed) ? parsed : null;
}

function resolveRescueMapStatus(value: string | null | undefined): RescueMapStatus | null {
  const normalized = normalizeTrangThaiPhieu(value);
  if (!normalized) {
    return null;
  }
  if (normalized === "DANG_TREN_DUONG_TOI") {
    return "DANG_TREN_DUONG_TOI";
  }
  if (normalized === "DANG_XU_LY" || normalized === "IN_PROGRESS" || normalized === "PROCESSING") {
    return "DANG_XU_LY";
  }
  if (normalized === "HOAN_THANH" || normalized === "COMPLETED" || normalized === "DONE") {
    return "HOAN_THANH";
  }
  return null;
}

function getRescueMarkerClassName(status: RescueMapStatus): string {
  switch (status) {
    case "DANG_XU_LY":
      return "rescue-map-node rescue-map-node--yellow";
    case "HOAN_THANH":
      return "rescue-map-node rescue-map-node--green";
    case "DANG_TREN_DUONG_TOI":
    default:
      return "rescue-map-node rescue-map-node--red";
  }
}

function createRescueMarkerElement(status: RescueMapStatus): HTMLDivElement {
  const element = document.createElement("div");
  element.className = getRescueMarkerClassName(status);
  element.innerHTML = `
    <span class="rescue-map-node__pulse rescue-map-node__pulse--1"></span>
    <span class="rescue-map-node__pulse rescue-map-node__pulse--2"></span>
    <span class="rescue-map-node__core"></span>
  `;
  return element;
}

function toMapNodes(phieuList: PhieuCuuTroDto[]): Array<{ lat: number; lng: number; status: RescueMapStatus }> {
  return phieuList
    .map((phieu) => {
      const status = resolveRescueMapStatus(phieu.trangThai);
      const lat = parseCoordinate(phieu.viTri?.lat);
      const lng = parseCoordinate(phieu.viTri?.longitude);
      if (!status || lat === null || lng === null) {
        return null;
      }
      if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
        return null;
      }
      return { status, lat, lng };
    })
    .filter((item): item is { lat: number; lng: number; status: RescueMapStatus } => item !== null);
}

function getRescueMapErrorMessage(error: unknown): string {
  if (error instanceof NguoiDungApiError) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return "Khong the tai vi tri phieu cuu tro.";
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
  const rescueMarkersRef = useRef<TrackAsiaMarkerInstance[]>([]);

  const [mapReady, setMapReady] = useState(false);
  const [mapError, setMapError] = useState("");
  const [locationStatus, setLocationStatus] = useState<LocationStatus>("idle");
  const [locationMessage, setLocationMessage] = useState("");
  const [rescueMessage, setRescueMessage] = useState("");

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
      rescueMarkersRef.current.forEach((marker) => marker.remove());
      rescueMarkersRef.current = [];
      markerRef.current?.remove();
      markerRef.current = null;
      mapRef.current?.remove();
      mapRef.current = null;
    };
  }, []);

  useEffect(() => {
    if (!mapReady || !mapRef.current) {
      return;
    }

    let cancelled = false;
    setRescueMessage("Dang tai vi tri phieu cuu tro...");
    ensureRescueNodeStyles();

    const renderRescueMarkers = () => {
      const map = mapRef.current;
      const trackasia = window.trackasiagl;
      const MarkerCtor = trackasia?.Marker;
      if (!map || !MarkerCtor) {
        setRescueMessage("Map marker chua san sang.");
        return;
      }

      fetchPhieuCuuTroList()
        .then((phieuList) => {
          if (cancelled) {
            return;
          }

          const nodes = toMapNodes(phieuList);
          rescueMarkersRef.current.forEach((marker) => marker.remove());
          rescueMarkersRef.current = [];

          nodes.forEach((node) => {
            const markerElement = createRescueMarkerElement(node.status);
            const marker = new MarkerCtor({ element: markerElement })
              .setLngLat([node.lng, node.lat])
              .addTo(map);
            rescueMarkersRef.current.push(marker);
          });

          setRescueMessage(
            nodes.length > 0
              ? `Dang hien thi ${nodes.length} vi tri phieu cuu tro.`
              : "Khong co vi tri phieu cuu tro phu hop de hien thi."
          );
        })
        .catch((error) => {
          if (cancelled) {
            return;
          }
          rescueMarkersRef.current.forEach((marker) => marker.remove());
          rescueMarkersRef.current = [];
          setRescueMessage(getRescueMapErrorMessage(error));
        });
    };

    void renderRescueMarkers();
    return () => {
      cancelled = true;
    };
  }, [mapReady]);

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

              {rescueMessage && (
                <span className="text-theme-xs text-gray-600 dark:text-gray-300" aria-live="polite">
                  {rescueMessage}
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
