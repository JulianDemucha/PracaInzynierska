import React from 'react'

import TopBar from './components/layout/TopBar.jsx'
import SideBar from './components/layout/SideBar.jsx'
import PlayerBar from './components/layout/PlayerBar.jsx'
import MainContent from './components/layout/MainContent.jsx'
import AuthModal from './components/auth/AuthModal.jsx'
import './App.css'

function App() {
    return (
        <div className="app-container">
            <SideBar />
            <TopBar />
            <MainContent />
            <PlayerBar />
            <AuthModal />
        </div>
    )
}

export default App