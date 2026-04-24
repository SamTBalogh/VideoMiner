import { createBrowserRouter } from "react-router-dom";
import { AppLayout } from "./ui/AppLayout";
import { IngestionPage } from "./ui/IngestionPage";
import { EndpointStudioPage } from "./ui/EndpointStudioPage";
import { DataExplorerPage } from "./ui/DataExplorerPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <AppLayout />,
    children: [
      { index: true, element: <IngestionPage /> },
      { path: "studio", element: <EndpointStudioPage /> },
      { path: "explorer", element: <DataExplorerPage /> },
    ],
  },
]);
