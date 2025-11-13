import * as React from 'react';
import {Outlet} from "react-router-dom";
import './MainContent.css'

function MainContent() {
    return(
        <main className="main-content custom-scrollbar">
            <Outlet />
        </main>
    )
}
export default MainContent