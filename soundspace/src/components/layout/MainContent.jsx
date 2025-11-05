import * as React from 'react';
import {Outlet} from "react-router-dom";
import './MainContent.css'

function MainContent() {
    return(
        <main className="main-content">
            <Outlet/>
        </main>
    )
}
export default MainContent