import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, of} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import { User } from '../models/User';


const API_ENDPOINT = 'http://localhost:3001/users';


@Injectable({
    providedIn: 'root'
})
export class UsersService {

    private isAuthenticated = false;
    private currentUser: any = null;

    constructor(private httpClient: HttpClient) {
    }

    // Check if credentials are correct
    checkCredentials(login: string, password: string): Observable<boolean> {
        return this.httpClient.get<User[]>(API_ENDPOINT).pipe(
            map((users: User[]) => {
                const user: User | undefined = users.find((u) => u.email === login && u.password === password);
                if (user) {
                    this.isAuthenticated = true;
                    this.currentUser = user; // Store the authenticated user's information
                }
                return !!user; // Return true if user exists, false otherwise
            }),
            catchError(() => of(false)) // Handle errors gracefully
        );
    }


    addUser(newUser: User): Observable<any> {
        return this.httpClient.post<any>(API_ENDPOINT, newUser);
    }

    getAllUsers(): Observable<any> {
        return this.httpClient.get<any>(API_ENDPOINT);
    }

    getCurrentUser(): User {
        return this.currentUser;
    }

    authenticateUser(): void {
        this.isAuthenticated = true;
    }

    isUserAuthenticated(): boolean {
        return this.isAuthenticated;
    }

    logoutUser(): void {
        this.isAuthenticated = false;
    }
}
