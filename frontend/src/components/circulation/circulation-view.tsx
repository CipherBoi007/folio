"use client";

import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { PageHeader } from "@/components/common/page-header";
import { IssueDesk } from "@/components/circulation/issue-desk";
import { ReturnDesk } from "@/components/circulation/return-desk";
import { useUrlState } from "@/hooks/use-url-state";

export function CirculationView() {
  const url = useUrlState();
  const tab = url.get("tab") === "return" ? "return" : "issue";

  return (
    <>
      <PageHeader title="Circulation" description="Issue books to members and record returns." />
      <Tabs value={tab} onValueChange={(value) => url.set({ tab: value === "return" ? "return" : null, q: null })}>
        <TabsList>
          <TabsTrigger value="issue">Issue</TabsTrigger>
          <TabsTrigger value="return">Return</TabsTrigger>
        </TabsList>
        <TabsContent value="issue" className="pt-2">
          <IssueDesk />
        </TabsContent>
        <TabsContent value="return" className="pt-2">
          <ReturnDesk />
        </TabsContent>
      </Tabs>
    </>
  );
}
