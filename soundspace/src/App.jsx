import React from 'react'

import TopBar from './components/layout/TopBar.jsx'
import SideBar from './components/layout/SideBar.jsx'
import PlayerBar from './components/layout/PlayerBar.jsx'
import MainContent from './components/layout/MainContent.jsx'


function App() {
    return (
        <div className = "app-container">
            <TopBar />
            <PlayerBar />
            <SideBar />
            <MainContent />
        </div>
    )
}

export default App