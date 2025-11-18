import React from 'react'
import ReactDOM from 'react-dom/client'
import {createBrowserRouter, RouterProvider} from 'react-router-dom'
import HomePage from './pages/HomePage'
import App from './App'
import './index.css'

import { PlayerProvider } from './context/PlayerContext.jsx';
import AuthProvider from './context/AuthProvider.jsx'
import ProfilePage from './pages/ProfilePage'
import ArtistPage from './pages/ArtistPage'
import SongPage from './pages/SongPage.jsx'
import CollectionPage from './pages/CollectionPage'

const router = createBrowserRouter([
    {
        path: "/",
        element: <App/>, children: [
            {
                index: true,
                element: <HomePage/>,
            },
            {
                path: "/profile",
                element: <ProfilePage />,
            },
            {
                path: "/artist/:id",
                element: <ArtistPage />
            },
            {
                path: "/song/:id",
                element: <SongPage />
            },
            {
                path: "/album/:id",
                element: <CollectionPage />
            },
            {
                path: "/playlist/:id",
                element: <CollectionPage />
            }
        ]
    }
]);

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <PlayerProvider>
            <AuthProvider>
                <RouterProvider router={router}/>
            </AuthProvider>
        </PlayerProvider>
    </React.StrictMode>,
)