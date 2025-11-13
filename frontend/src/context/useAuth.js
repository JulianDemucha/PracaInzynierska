import { useContext } from "react";
import { AuthContext } from "./authContext.js";

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) throw new Error("useAuth musi byc wywolane w AuthProvider");
    return context;
}