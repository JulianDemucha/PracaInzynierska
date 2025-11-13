import React from 'react'
import ReactDOM from 'react-dom/client'
import {createBrowserRouter, RouterProvider} from 'react-router-dom'
import HomePage from './pages/HomePage'
import App from './App'
import './index.css'
import AuthProvider from './context/AuthContext'
import ProfilePage from './pages/ProfilePage'

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
            }
        ]
    }
]);

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <AuthProvider>
            <RouterProvider router={router}/>
        </AuthProvider>
    </React.StrictMode>,
)